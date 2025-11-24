package it.xeniaprogetti.rfi.plugin.example.events.pbh;

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

@Component(name = "pbhEventIngestor",
        immediate = true)
public class PbhEventIngestor implements EventListener {

    private static final Logger log = LoggerFactory.getLogger(PbhEventIngestor.class);

    private static final String UEI_PBH_PREFIX = "uei.opennms.org/traps/PBH-MIB-ALARM";
    private static final String NODE_LABEL_PBH_PARAMETER_MATCH = ".1.3.6.1.4.1.46302.109.1.2";
    protected static final String TIME_PBH_PARAMETER = ".1.3.6.1.4.1.46302.109.1.9"; //AEMUniqueRef
    protected static final DateTimeFormatter TRAP_TIME_FORMATTER = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
    /*protected static final DateTimeFormatter TRAP_TIME_FORMATTER = new DateTimeFormatterBuilder().parseCaseInsensitive()
                    .appendPattern("EEE MMM dd HH:mm:ss z yyyy")
                    .toFormatter(Locale.ENGLISH);*/
    protected static final List<String> INTERESTING_PBH_UEIS = Arrays.asList(
            UEI_PBH_PREFIX + "/pbhEventTrapAlarmIndeterminate",
            UEI_PBH_PREFIX + "/pbhEventTrapAlarmInformational",
            UEI_PBH_PREFIX + "/pbhEventTrapAlarmWarning",
            UEI_PBH_PREFIX + "/pbhEventTrapAlarmMinor",
            UEI_PBH_PREFIX + "/pbhEventTrapAlarmMajor",
            UEI_PBH_PREFIX + "/pbhEventTrapAlarmCritical",
            UEI_PBH_PREFIX + "/pbhEventTrapClear",
            UEI_PBH_PREFIX + "/pbhEventTrapNotificationIndeterminate",
            UEI_PBH_PREFIX + "/pbhEventTrapNotificationInformational",
            UEI_PBH_PREFIX + "/pbhEventTrapNotificationWarning",
            UEI_PBH_PREFIX + "/pbhEventTrapNotificationMinor",
            UEI_PBH_PREFIX + "/pbhEventTrapNotificationMajor",
            UEI_PBH_PREFIX + "/pbhEventTrapNotificationCritical"
    );

    private EventForwarder eventForwarder;
    private NodeDao nodeDao;
    private EventSubscriptionService eventSubscriptionService;

    public PbhEventIngestor() {
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
        eventSubscriptionService.addEventListener(this, INTERESTING_PBH_UEIS);
        log.info("PbhEventIngestor registered on UEIS: {}", INTERESTING_PBH_UEIS);
    }

    @Deactivate
    public void deactivate(){
        try {
            eventSubscriptionService.removeEventListener(this, INTERESTING_PBH_UEIS);
            log.info("PbhEventIngestor deregistered");
        } catch (Exception e) {
            log.warn("Error while deregistering listener", e);
        }
    }

    @Override
    public String getName() {
        return "pbhEventIngestor";
    }

    @Override
    public int getNumThreads() {
        return 1;
    }

    @Override
    public void onEvent(InMemoryEvent e) {

        log.info("Arrived new event filtered to PbhIngestorComponent: {}", e);

        ImmutableInMemoryEvent translate = translate(e);

        log.info("send event translated: {} ", translate);
        eventForwarder.sendAsync(translate);
    }

    protected ImmutableInMemoryEvent translate(InMemoryEvent e) {

        String nodeLabel = e.getParametersByName(NODE_LABEL_PBH_PARAMETER_MATCH).stream()
                .findFirst().map(EventParameter::getValue).orElse(null);

        Node node = nodeLabel == null ? null : nodeDao.getNodeByLabel(nodeLabel);
        int nodeId = node == null ? e.getNodeId() : node.getId();

        String uei = e.getUei().replace("/traps/", "/translator/");

        ImmutableInMemoryEvent.Builder builder = ImmutableInMemoryEvent.newBuilderFrom(e)
                .setNodeId(nodeId)
                .setUei(uei)
                .setSource("rfi-plugin-example");

        String timeEvent = e.getParametersByName(TIME_PBH_PARAMETER).stream()
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
