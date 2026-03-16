package it.xeniaprogetti.rfi.plugin.example.events.acom;

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

@Component(name = "aComEventIngestor",
            immediate = true)
public class AComEventIngestorComponent implements EventListener {

    private static final Logger log = LoggerFactory.getLogger(AComEventIngestorComponent.class);

    private static final String UEI_ACOM_PREFIX = "uei.opennms.org/traps/INC-MIB-AL";
    private static final String NODE_LABEL_ACOM_PARAMETER_MATCH = ".1.3.6.1.4.1.231.7.99.4.2.1.1.11";
    protected static final String TIME_ACOM_PARAMETER = ".1.3.6.1.4.1.231.7.99.4.2.1.1.1"; //tiAlarmDateTime
    protected static final DateTimeFormatter TRAP_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssZ");
    public static final List<String> INTERESTING_ACOM_UEIS = Arrays.asList(
            UEI_ACOM_PREFIX + "/tiIncTrapCleared",
            UEI_ACOM_PREFIX + "/tiIncTrapNormal",
            UEI_ACOM_PREFIX + "/tiIncTrapWarning",
            UEI_ACOM_PREFIX + "/tiIncTrapMinor",
            UEI_ACOM_PREFIX + "/tiIncTrapMajor",
            UEI_ACOM_PREFIX + "/tiIncTrapCritical"
    );

    private EventForwarder eventForwarder;
    private NodeDao nodeDao;
    private EventSubscriptionService eventSubscriptionService;

    public AComEventIngestorComponent() {
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
        eventSubscriptionService.addEventListener(this, INTERESTING_ACOM_UEIS);
        log.info("AComEventIngestorComponent registered on UEIS: {}", INTERESTING_ACOM_UEIS);
    }

    @Deactivate
    public void deactivate(){
        try {
            eventSubscriptionService.removeEventListener(this, INTERESTING_ACOM_UEIS);
            log.info("AComEventIngestorComponent deregistered");
        } catch (Exception e) {
            log.warn("Error while deregistering listener", e);
        }
    }

    @Override
    public String getName() {
        return "acomEventIngestor";
    }

    @Override
    public int getNumThreads() {
        return 1;
    }

    @Override
    public void onEvent(InMemoryEvent e) {

        log.info("Arrived new event filtered to AComEventIngestorComponent: {}", e);

        ImmutableInMemoryEvent translate = translate(e);

        log.info("send event translated: {} ", translate);
        eventForwarder.sendAsync(translate);
    }

    protected ImmutableInMemoryEvent translate(InMemoryEvent e) {

        String nodeLabel = e.getParametersByName(NODE_LABEL_ACOM_PARAMETER_MATCH).stream()
                .findFirst().map(EventParameter::getValue).orElse(null);

        Node node = nodeLabel == null ? null : nodeDao.getNodeByLabel(nodeLabel);

        String uei = e.getUei().replace("/traps/", "/translator/");

        ImmutableInMemoryEvent.Builder builder = ImmutableInMemoryEvent.newBuilderFrom(e)
                .setNodeId(node == null ? 1 : node.getId())
                .setUei(uei)
                .setSource("rfi-plugin-example");

        String timeEvent = e.getParametersByName(TIME_ACOM_PARAMETER).stream()
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
