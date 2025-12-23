package it.xeniaprogetti.rfi.plugin.example.events.axell;

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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component(name = "axellEventIngestor",
        immediate = true)
public class AxellEventIngestor implements EventListener {

    private static final Logger log = LoggerFactory.getLogger(AxellEventIngestor.class);

    private static final String UEI_AXELL_PREFIX = "uei.opennms.org/traps/AEM3-MIB";
    private static final String NODE_LABEL_AXELL_PARAMETER_MATCH = ".1.3.6.1.4.1.8829.100.1.2.6";
    protected static final String TIME_AXELL_PARAMETER = ".1.3.6.1.4.1.8829.100.1.2.4"; //AEMUniqueRef
    protected static final DateTimeFormatter TRAP_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    public static final List<String> INTERESTING_AXELL_UEIS = Arrays.asList(
            UEI_AXELL_PREFIX + "/aemAlarmNotificationIndeterminate",
            UEI_AXELL_PREFIX + "/aemAlarmNotificationCritical",
            UEI_AXELL_PREFIX + "/aemAlarmNotificationMajor",
            UEI_AXELL_PREFIX + "/aemAlarmNotificationMinor",
            UEI_AXELL_PREFIX + "/aemAlarmNotificationWarning",
            UEI_AXELL_PREFIX + "/aemAlarmClearanceNotification"
    );

    private EventForwarder eventForwarder;
    private NodeDao nodeDao;
    private EventSubscriptionService eventSubscriptionService;

    public AxellEventIngestor() {
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
        eventSubscriptionService.addEventListener(this, INTERESTING_AXELL_UEIS);
        log.info("AxellEventIngestor registered on UEIS: {}", INTERESTING_AXELL_UEIS);
    }

    @Deactivate
    public void deactivate(){
        try {
            eventSubscriptionService.removeEventListener(this, INTERESTING_AXELL_UEIS);
            log.info("AxellEventIngestor deregistered");
        } catch (Exception e) {
            log.warn("Error while deregistering listener", e);
        }
    }

    @Override
    public String getName() {
        return "axellEventIngestor";
    }

    @Override
    public int getNumThreads() {
        return 1;
    }

    @Override
    public void onEvent(InMemoryEvent e) {

        log.info("Arrived new event filtered to AxellIngestorComponent: {}", e);

        ImmutableInMemoryEvent translate = translate(e);

        log.info("send event translated: {} ", translate);
        eventForwarder.sendAsync(translate);
    }

    protected ImmutableInMemoryEvent translate(InMemoryEvent e) {

        String nodeLabel = e.getParametersByName(NODE_LABEL_AXELL_PARAMETER_MATCH).stream()
                .findFirst().map(EventParameter::getValue).orElse(null);

        Node node = nodeLabel == null ? null : nodeDao.getNodeByLabel(nodeLabel);
        int nodeId = node == null ? e.getNodeId() : node.getId();

        String uei = e.getUei().replace("/traps/", "/translator/");

        ImmutableInMemoryEvent.Builder builder = ImmutableInMemoryEvent.newBuilderFrom(e)
                .setNodeId(nodeId)
                .setUei(uei)
                .setSource("rfi-plugin-example");

        String timeEvent = e.getParametersByName(TIME_AXELL_PARAMETER).stream()
                .findFirst().map(EventParameter::getValue).orElse(null);

        if(timeEvent!=null){

            try{
                LocalDateTime localDateTime = LocalDateTime.parse(timeEvent, TRAP_TIME_FORMATTER);
                Instant trapInstant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
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
