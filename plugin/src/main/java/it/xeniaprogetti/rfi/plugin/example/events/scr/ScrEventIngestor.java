package it.xeniaprogetti.rfi.plugin.example.events.scr;

import org.opennms.integration.api.v1.dao.NodeDao;
import org.opennms.integration.api.v1.events.EventForwarder;
import org.opennms.integration.api.v1.events.EventListener;
import org.opennms.integration.api.v1.events.EventSubscriptionService;
import org.opennms.integration.api.v1.model.EventParameter;
import org.opennms.integration.api.v1.model.InMemoryEvent;
import org.opennms.integration.api.v1.model.Node;
import org.opennms.integration.api.v1.model.immutables.ImmutableInMemoryEvent;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Component(name = "scrEventIngestor",
        immediate = true)
public class ScrEventIngestor implements EventListener {

    private static final Logger log = LoggerFactory.getLogger(ScrEventIngestor.class);

    private static final String UEI_SCR_PREFIX = "uei.opennms.org/traps/SCAIR-MIB-ALARM";
    private static final String NODE_LABEL_SCR_PARAMETER_MATCH = ".1.3.6.1.4.1.46302.109.1.2";
    protected static final String TIME_SCR_PARAMETER = ".1.3.6.1.4.1.46302.109.1.9"; //AEMUniqueRef
    protected static final DateTimeFormatter TRAP_TIME_FORMATTER = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
    public static final List<String> INTERESTING_SCR_UEIS = Arrays.asList(
            UEI_SCR_PREFIX + "/scairEventTrapAlarmIndeterminate",
            UEI_SCR_PREFIX + "/scairEventTrapAlarmInformational",
            UEI_SCR_PREFIX + "/scairEventTrapAlarmWarning",
            UEI_SCR_PREFIX + "/scairEventTrapAlarmMinor",
            UEI_SCR_PREFIX + "/scairEventTrapAlarmMajor",
            UEI_SCR_PREFIX + "/scairEventTrapAlarmCritical",
            UEI_SCR_PREFIX + "/scairEventTrapClear",
            UEI_SCR_PREFIX + "/scairEventTrapNotificationIndeterminate",
            UEI_SCR_PREFIX + "/scairEventTrapNotificationInformational",
            UEI_SCR_PREFIX + "/scairEventTrapNotificationWarning",
            UEI_SCR_PREFIX + "/scairEventTrapNotificationMinor",
            UEI_SCR_PREFIX + "/scairEventTrapNotificationMajor",
            UEI_SCR_PREFIX + "/scairEventTrapNotificationCritical"
    );

    private EventForwarder eventForwarder;
    private NodeDao nodeDao;
    private EventSubscriptionService eventSubscriptionService;

    public ScrEventIngestor() {
    }

    @Reference
    public void setEventForwarder(EventForwarder eventForwarder) {
        this.eventForwarder = eventForwarder;
    }
    @Reference
    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }
    @Reference
    public void setEventSubscriptionService(EventSubscriptionService eventSubscriptionService) {
        this.eventSubscriptionService = eventSubscriptionService;
    }

    @Activate
    public void activate(){
        eventSubscriptionService.addEventListener(this, INTERESTING_SCR_UEIS);
        log.info("ScairEventIngestor registered on UEIS: {}", INTERESTING_SCR_UEIS);
    }

    @Deactivate
    public void deactivate(){
        try {
            eventSubscriptionService.removeEventListener(this, INTERESTING_SCR_UEIS);
            log.info("ScrEventIngestor deregistered");
        } catch (Exception e) {
            log.warn("Error while deregistering listener", e);
        }
    }

    @Override
    public String getName() {
        return "scairEventIngestor";
    }

    @Override
    public int getNumThreads() {
        return 1;
    }

    @Override
    public void onEvent(InMemoryEvent e) {

        log.info("Arrived new event filtered to ScrEventIngestorComponent: {}", e);

        ImmutableInMemoryEvent translate = translate(e);

        log.info("send event translated: {} ", translate);
        eventForwarder.sendAsync(translate);
    }

    protected ImmutableInMemoryEvent translate(InMemoryEvent e) {

        String nodeLabel = e.getParametersByName(NODE_LABEL_SCR_PARAMETER_MATCH).stream()
                .findFirst().map(EventParameter::getValue).orElse(null);

        Node node = nodeLabel == null ? null : nodeDao.getNodeByLabel(nodeLabel);
        int nodeId = node == null ? e.getNodeId() : node.getId();

        String uei = e.getUei().replace("/traps/", "/translator/");

        ImmutableInMemoryEvent.Builder builder = ImmutableInMemoryEvent.newBuilderFrom(e)
                .setNodeId(nodeId)
                .setUei(uei)
                .setSource("rfi-plugin-example");

        String timeEvent = e.getParametersByName(TIME_SCR_PARAMETER).stream()
                .findFirst().map(EventParameter::getValue).orElse(null);

        if(timeEvent!=null){

            try{
                Instant trapInstant = ZonedDateTime.parse(timeEvent, TRAP_TIME_FORMATTER).toInstant();
                Date trapDate = Date.from(trapInstant);
                builder.setTime(trapDate);
            } catch (DateTimeParseException ex) {
                log.error("Unable to parse event time '{}', keeping original event time", timeEvent, ex);
            }

        }else{
            log.info("The event time into trap is null.");
        }

        return builder.build();
    }


}
