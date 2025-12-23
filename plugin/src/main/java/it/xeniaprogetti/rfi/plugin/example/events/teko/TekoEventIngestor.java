package it.xeniaprogetti.rfi.plugin.example.events.teko;

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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component(name = "tekoEventIngestor",
        immediate = true)
public class TekoEventIngestor implements EventListener {

    private static final Logger log = LoggerFactory.getLogger(TekoEventIngestor.class);

    private static final String UEI_TEKO_PREFIX = "uei.opennms.org/traps/TEKOTELECOM-OMC-MIB";
    private static final String NODE_LABEL_TEKO_PARAMETER_MATCH = ".1.3.6.1.4.1.6626.6000.2.2.3.2.1.4";
    protected static final String OID_TEKO_TIME_ACTIVE = ".1.3.6.1.4.1.6626.6000.2.2.3.2.1.2"; //AEMUniqueRef
    protected static final String OID_TEKO_TIME_CLEAR = ".1.3.6.1.4.1.6626.6000.2.2.4.2.1.2";
    private static final String OID_TEKO_FOREIGNID = ".1.3.6.1.4.1.6626.6000.2.2.1.1.1.4";
    private static final String OID_TEKO_TF        = ".1.3.6.1.4.1.6626.6000.2.2.1.1.1.9";
    private static final String OID_TEKO_PATH      = ".1.3.6.1.4.1.6626.6000.2.2.3.2.1.4";//AEMUniqueRef
    protected static final String REGEX_TEKO = "/([^/]*)/([^/]*)/(.*)$";
    protected static final String REGEX_SHORT_TEKO = "/([^/]*)/(.*)$";
    protected static final String CLEAR_UEI_TEKO = UEI_TEKO_PREFIX + "/omcClearState";
    public static final List<String> INTERESTING_TEKO_UEIS = Arrays.asList(
            UEI_TEKO_PREFIX + "/omcActiveStateIndeterminate",
            UEI_TEKO_PREFIX + "/omcActiveStateCritical",
            UEI_TEKO_PREFIX + "/omcActiveStateMajor",
            UEI_TEKO_PREFIX + "/omcActiveStateMinor",
            UEI_TEKO_PREFIX + "/omcActiveStateWarning",
            CLEAR_UEI_TEKO
    );
    private EventForwarder eventForwarder;
    private NodeDao nodeDao;
    private EventSubscriptionService eventSubscriptionService;

    public TekoEventIngestor(){
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
        eventSubscriptionService.addEventListener(this, INTERESTING_TEKO_UEIS);
        log.info("TekoEventIngestor registered on UEIS: {}", INTERESTING_TEKO_UEIS);
    }

    @Deactivate
    public void deactivate(){
        try {
            eventSubscriptionService.removeEventListener(this, INTERESTING_TEKO_UEIS);
            log.info("TekoEventIngestor deregistered");
        } catch (Exception e) {
            log.warn("Error while deregistering listener", e);
        }
    }

    @Override
    public String getName() {
        return "tekoEventIngestor";
    }

    @Override
    public int getNumThreads() {
        return 1;
    }

    @Override
    public void onEvent(InMemoryEvent e) {

        log.info("Arrived new event filtered to TekoEventIngestor: {}", e);

        ImmutableInMemoryEvent translate;
        if(CLEAR_UEI_TEKO.equals(e.getUei())){
            translate = translateClear(e);
        }else{
            translate = translateActivate(e);
        }

        log.info("send event translated: {} ", translate);
        eventForwarder.sendAsync(translate);
    }

    protected ImmutableInMemoryEvent translateActivate(InMemoryEvent e) {

        String uei = e.getUei().replace("/traps/", "/translator/");

        log.debug("active event parameter: {}", e.getParameters());
        Node node = getNode(e);
        int nodeId = node == null ? e.getNodeId() : node.getId();

        ImmutableInMemoryEvent.Builder builder = ImmutableInMemoryEvent.newBuilderFrom(e)
                .setNodeId(nodeId)
                .setUei(uei)
                .setSource("rfi-plugin-example");

        String timeEvent = e.getParametersByName(OID_TEKO_TIME_ACTIVE).stream()
                .findFirst().map(EventParameter::getValue).orElse(null);

        if(timeEvent!=null){

            try{
                Date trapDate = parseSnmpDateAndTime(timeEvent);
                builder.setTime(trapDate);

            } catch (NumberFormatException ex) {
                log.error("Invalid UNIX timestamp value '{}', keeping original event time", timeEvent, ex);
            }

        }else{
            log.info("The event time into trap is null.");
        }

        return builder.build();
    }

    protected ImmutableInMemoryEvent translateClear(InMemoryEvent e) {

        String uei = e.getUei().replace("/traps/", "/translator/");

        log.debug("clear event parameter: {}", e.getParameters());

        ImmutableInMemoryEvent.Builder builder = ImmutableInMemoryEvent.newBuilderFrom(e)
                .setNodeId(e.getNodeId()) //????
                .setUei(uei)
                .setSource("rfi-plugin-example");

        String timeEvent = e.getParametersByName(OID_TEKO_TIME_CLEAR).stream()
                .findFirst().map(EventParameter::getValue).orElse(null);

        if(timeEvent!=null){

            try{
                Date trapDate = parseSnmpDateAndTime(timeEvent);
                builder.setTime(trapDate);

            } catch (NumberFormatException ex) {
                log.error("Invalid UNIX timestamp value '{}', keeping original event time", timeEvent, ex);
            }

        }else{
            log.info("The event time into trap is null.");
        }

        return builder.build();
    }

    private Node getNode(InMemoryEvent e) {

        Node node;
        String foreignId = getParam(e, OID_TEKO_FOREIGNID);

        if(foreignId == null){
            log.debug("foreingId is null into event.");
            return null;
        } else {
            log.debug("foreingId: {}", foreignId);
            node = nodeDao.getNodeByLabel(foreignId);
            if(node != null){
                log.debug("node with foreigniId: {} found!", foreignId);
                return node;
            } else{
                log.debug("node with foreigniId: {} not found!", foreignId);

                String tf = getParam(e, OID_TEKO_TF);
                String path = getParam(e, OID_TEKO_PATH);

                if(tf == null || path == null){
                    log.debug("'tf' or 'path' is null into event.");
                    return null;
                }else{
                    log.debug("path: {} - tf: {}", path, tf);
                    if("tf".equalsIgnoreCase(tf)){
                        log.debug("parameter .2.1.1.1.9 == tf");
                        log.debug("checking if node with nodelabel == group 2 of regex is present ...");
                        String label1 = matchRegex(path, REGEX_TEKO, 2);
                        node = nodeDao.getNodeByLabel(label1);
                        if(node != null){
                            log.debug("node: {} found!", node);
                            return node;
                        }else{
                            log.debug("node not found ...");
                            log.debug("checking if node with nodelabel == group 1 of regex is present ...");
                            String label2 = matchRegex(path, REGEX_TEKO, 1);
                            node = nodeDao.getNodeByLabel(label2);

                            if(node != null){
                                log.debug("node found {}", node);
                            }else{
                                log.debug("node not found!");
                            }
                            return node;
                        }
                    }else{
                        log.debug("parameter .2.1.1.1.9 != tf");
                        log.debug("checking if node with nodelabel == group 1 of short regex is present ...");
                        String labelShort = matchRegex(path, REGEX_SHORT_TEKO, 1);
                        node = nodeDao.getNodeByLabel(labelShort);
                        if(node != null){
                            log.debug("node: {} found!", node);
                        }else{
                            log.debug("node not found!");
                        }
                        return node;
                    }
                }
            }
        }

        /*if(foreignId != null){
            log.debug("foreing id {}", foreignId);
            node = nodeDao.getNodeByLabel(foreignId);
        } else{
            log.debug("foreing id null into trap");
            String tf = getParam(e, OID_TEKO_TF);
            String path = getParam(e, OID_TEKO_PATH);

            log.debug("path: {} - tf: {}", path, tf);

            if(tf.equalsIgnoreCase("tf")){
                String label1 = matchRegex(path, REGEX_TEKO, 2);

                log.debug("value group 2 regex: {}", label1);

                if(label1 != null){
                    node = nodeDao.getNodeByLabel(label1);
                }else{

                    String label2 = matchRegex(path, REGEX_TEKO, 1);

                    log.debug("value group 2 is null ... value group 1 regex: {}", label2);
                    node = nodeDao.getNodeByLabel(label2);
                }
            }else{
                String labelShort = matchRegex(path, REGEX_SHORT_TEKO, 1);
                log.debug("parameter .9 != tf, value group 1 short regex: {}", labelShort);
                node = nodeDao.getNodeByLabel(labelShort);
            }
        }*/
    }

    private String getParam(InMemoryEvent e, String oid) {
        return e.getParametersByName(oid)
                .stream()
                .findFirst()
                .map(EventParameter::getValue)
                .orElse(null);
    }

    private String matchRegex(String text, String regex, int group) {

        if (text == null)
            return null;

        Matcher m = Pattern.compile(regex).matcher(text);
        if (m.matches())
            return m.group(group);

        return null;
    }

    private Date parseSnmpDateAndTime(String hex) {
        ZonedDateTime zdt = decodeSnmpDateAndTime(hex);
        return Date.from(zdt.toInstant());
    }

    private ZonedDateTime decodeSnmpDateAndTime(String hex) {
        byte[] bytes = hexStringToByteArray(hex);

        int year    = ((bytes[0] & 0xFF) << 8) | (bytes[1] & 0xFF);
        int month   = bytes[2] & 0xFF;
        int day     = bytes[3] & 0xFF;
        int hour    = bytes[4] & 0xFF;
        int minute  = bytes[5] & 0xFF;
        int second  = bytes[6] & 0xFF;
        int deci    = bytes[7] & 0xFF;

        char sign   = (char) bytes[8];
        int tzHour  = bytes[9] & 0xFF;
        int tzMin   = bytes[10] & 0xFF;

        int offsetMinutes = tzHour * 60 + tzMin;
        if (sign == '-') offsetMinutes = -offsetMinutes;

        ZoneOffset offset = ZoneOffset.ofTotalSeconds(offsetMinutes * 60);

        return ZonedDateTime.of(
                year, month, day,
                hour, minute, second,
                deci * 100_000_000,   // decisecondi → nanosecondi
                offset
        );
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) (
                    (Character.digit(s.charAt(i), 16) << 4) +
                            Character.digit(s.charAt(i + 1), 16)
            );
        }
        return data;
    }

}
