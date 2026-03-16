package it.xeniaprogetti.rfi.plugin.example.events.smarts94;

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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component(name = "smarts94EventIngestor",
        immediate = true)
public class Smarts94EventIngestor implements EventListener {

    private static final Logger log = LoggerFactory.getLogger(Smarts94EventIngestor.class);

    private static final String UEI_SMARTS94_PREFIX = "uei.opennms.org/traps/SMARTS-94-MIB";
    private static final String NODE_LABEL_SMARTS94_PARAMETER_MATCH = ".1.3.6.1.4.1.733.2.1.10";
    protected static final String TIME_SMARTS94_PARAMETER = ".1.3.6.1.4.1.733.2.1.1"; //AEMUniqueRef
    //protected static final DateTimeFormatter TRAP_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final List<String> INTERESTING_SMARTS94_UEIS = Arrays.asList(
            UEI_SMARTS94_PREFIX + "/smTrapNotificationInformational",
            UEI_SMARTS94_PREFIX + "/smTrapNotificationWarning",
            UEI_SMARTS94_PREFIX + "/smTrapNotificationMinor",
            UEI_SMARTS94_PREFIX + "/smTrapNotificationMajor",
            UEI_SMARTS94_PREFIX + "/smTrapNotificationCritical",
            UEI_SMARTS94_PREFIX + "/smTrapNotificationClear"
    );

    private EventForwarder eventForwarder;
    private NodeDao nodeDao;
    private EventSubscriptionService eventSubscriptionService;

    public Smarts94EventIngestor(){
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
        eventSubscriptionService.addEventListener(this, INTERESTING_SMARTS94_UEIS);
        log.info("Smarts94EventIngestor registered on UEIS: {}", INTERESTING_SMARTS94_UEIS);
    }

    @Deactivate
    public void deactivate(){
        try {
            eventSubscriptionService.removeEventListener(this, INTERESTING_SMARTS94_UEIS);
            log.info("Smarts94EventIngestor deregistered");
        } catch (Exception e) {
            log.warn("Error while deregistering listener", e);
        }
    }

    @Override
    public String getName() {
        return "smarts94EventIngestor";
    }

    @Override
    public int getNumThreads() {
        return 1;
    }

    @Override
    public void onEvent(InMemoryEvent e) {

        log.info("Arrived new event filtered to Smarts94EventIngestor: {}", e);

        ImmutableInMemoryEvent translate = translate(e);

        log.info("send event translated: {} ", translate);
        eventForwarder.sendAsync(translate);
    }

    protected ImmutableInMemoryEvent translate(InMemoryEvent e) {

        String nodeLabel = e.getParametersByName(NODE_LABEL_SMARTS94_PARAMETER_MATCH).stream()
                .findFirst().map(EventParameter::getValue).orElse(null);

        Node node = nodeLabel == null ? null : nodeDao.getNodeByLabel(nodeLabel);
        int nodeId = node == null ? e.getNodeId() : node.getId();

        String uei = e.getUei().replace("/traps/", "/translator/");

        ImmutableInMemoryEvent.Builder builder = ImmutableInMemoryEvent.newBuilderFrom(e)
                .setNodeId(nodeId)
                .setUei(uei)
                .setSource("rfi-plugin-example");

        String timeEvent = e.getParametersByName(TIME_SMARTS94_PARAMETER).stream()
                .findFirst().map(EventParameter::getValue).orElse(null);

        if(timeEvent!=null){

            try{
                long unixSeconds = Long.parseLong(timeEvent);

                Instant trapInstant = Instant.ofEpochSecond(unixSeconds);
                Date trapDate = Date.from(trapInstant);
                builder.setTime(trapDate);

            } catch (NumberFormatException ex) {
                log.error("Invalid UNIX timestamp value '{}', keeping original event time", timeEvent, ex);
            }

        }else{
            log.info("The event time into trap is null.");
        }

        return builder.build();
    }
}
