package it.xeniaprogetti.rfi.plugin.example.events.andrew;

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

@Component(name = "andrewEventIngestor",
        immediate = true)
public class AndrewEventIngestor implements EventListener {

    private static final Logger log = LoggerFactory.getLogger(AndrewEventIngestor.class);

    private static final String UEI_ANDREW_PREFIX = "uei.opennms.org/traps/MIKOM_OMC_Alarmforwarding-MIB";
    private static final String NODE_LABEL_ANDREW_PARAMETER_MATCH = ".1.3.6.1.4.1.6408.100.2.10.1.6";
    protected static final String OID_ANDREW_TRAP_PARAMETER = ".1.3.6.1.6.3.1.1.4.1.0";
    protected static final String TIME_ANDREW_PARAMETER = ".1.3.6.1.4.1.6408.100.2.10.1.7"; //nodeRaiseTime
    protected static final String TIME_ANDREW_CLEAR_PARAMETER = ".1.3.6.1.4.1.6408.100.2.10.1.8"; //nodeRaiseTime
    protected static final DateTimeFormatter TRAP_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd,HH:mm:ss");
    protected static final String CLEAR_UEI_ANDREW = UEI_ANDREW_PREFIX + "/mAlarmClearTrap";
    public static final List<String> INTERESTING_ANDREW_UEIS = Arrays.asList(
            UEI_ANDREW_PREFIX + "/mAlarmTrapIndeterminate",
            UEI_ANDREW_PREFIX + "/mAlarmTrapCritical",
            UEI_ANDREW_PREFIX + "/mAlarmTrapMajor",
            UEI_ANDREW_PREFIX + "/mAlarmTrapMinor",
            UEI_ANDREW_PREFIX + "/mAlarmTrapWarning",
            CLEAR_UEI_ANDREW
    );

    private EventForwarder eventForwarder;
    private NodeDao nodeDao;
    private EventSubscriptionService eventSubscriptionService;

    public AndrewEventIngestor() {
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
        eventSubscriptionService.addEventListener(this, INTERESTING_ANDREW_UEIS);
        log.info("AndrewEventIngestor registered on UEIS: {}", INTERESTING_ANDREW_UEIS);
    }

    @Deactivate
    public void deactivate(){
        try {
            eventSubscriptionService.removeEventListener(this, INTERESTING_ANDREW_UEIS);
            log.info("AndrewEventIngestor deregistered");
        } catch (Exception e) {
            log.warn("Error while deregistering listener", e);
        }
    }

    @Override
    public String getName() {
        return "andrewEventIngestor";
    }

    @Override
    public int getNumThreads() {
        return 1;
    }

    @Override
    public void onEvent(InMemoryEvent e) {

        log.info("Arrived new event filtered to AndrewIngestorComponent: {}", e);

        ImmutableInMemoryEvent translate = translate(e);

        log.info("send event translated: {} ", translate);
        eventForwarder.sendAsync(translate);
    }

    protected ImmutableInMemoryEvent translate(InMemoryEvent e) {

        String nodeLabel = e.getParametersByName(NODE_LABEL_ANDREW_PARAMETER_MATCH).stream()
                .findFirst().map(EventParameter::getValue).orElse(null);

        Node node = nodeLabel == null ? null : nodeDao.getNodeByLabel(nodeLabel);
        int nodeId = node == null ? e.getNodeId() : node.getId();

        String uei = e.getUei().replace("/traps/", "/translator/");

        ImmutableInMemoryEvent.Builder builder = ImmutableInMemoryEvent.newBuilderFrom(e)
                .setNodeId(nodeId)
                .setUei(uei)
                .setSource("rfi-plugin-example");

        String timeOid = null;
        if(CLEAR_UEI_ANDREW.equals(e.getUei())){
            timeOid = TIME_ANDREW_CLEAR_PARAMETER;
        }else{
            timeOid = TIME_ANDREW_PARAMETER;
        }

        String timeEvent = e.getParametersByName(timeOid).stream()
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
