package it.xeniaprogetti.rfi.plugin.example.provisioning.netact;

import it.xeniaprogetti.rfi.plugin.example.connection.Connection;
import it.xeniaprogetti.rfi.plugin.example.connection.ConnectionManager;
import org.opennms.integration.api.v1.config.requisition.RequisitionNode;
import org.opennms.integration.api.v1.config.requisition.immutables.ImmutableRequisition;
import org.opennms.integration.api.v1.config.requisition.immutables.ImmutableRequisitionMetaData;
import org.opennms.integration.api.v1.config.requisition.immutables.ImmutableRequisitionNode;
import org.opennms.integration.api.v1.dao.NodeDao;
import org.opennms.integration.api.v1.events.EventListener;
import org.opennms.integration.api.v1.events.EventSubscriptionService;
import org.opennms.integration.api.v1.model.EventParameter;
import org.opennms.integration.api.v1.model.InMemoryEvent;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component(name = "netActEventManager",
        immediate = true)
public class NetActEventManager implements EventListener {

    private static final Logger log = LoggerFactory.getLogger(NetActEventManager.class);
    private static final String UNDERSCORE_DELIMITER = "_";
    private static final String SLASH_DELIMITER = "/";
    protected static final String SEQUENCE_ID_NETACT_PARAMETER = ".1.3.6.1.4.1.28458.1.26.2.1.3.9";
    protected static final String OBJ_INSTANCE_PARAMETER = ".1.3.6.1.4.1.28458.1.26.2.1.6.5";
    protected static final String EVENT_TIME_NETACT_PARAMETER = ".1.3.6.1.4.1.28458.1.26.2.1.6.3";
    protected static final String OPT_INFO_NETACT_PARAMETER = ".1.3.6.1.4.1.28458.1.26.3.1.1.19";
    private static final String UEI_NETACT_PREFIX = "uei.opennms.org/traps/NSN-SNMP-NBI-TOPOLOGY-MIB";
    public static final List<String> INTERESTING_NETACT_UEIS = Arrays.asList(
            UEI_NETACT_PREFIX + "/nbiCreateNotification",
            UEI_NETACT_PREFIX + "/nbiDeleteNotification"
    );

    private EventSubscriptionService eventSubscriptionService;
    private NodeDao nodeDao;
    private ConnectionManager connectionManager;


    public NetActEventManager(){}

    @Reference
    public void setEventSubscriptionService(EventSubscriptionService eventSubscriptionService) {
        this.eventSubscriptionService = eventSubscriptionService;
    }
    @Reference
    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }
    @Reference
    public void setConnectionManager(ConnectionManager connectionManager){ this.connectionManager = connectionManager;}

    @Activate
    public void activate(){
        eventSubscriptionService.addEventListener(this, INTERESTING_NETACT_UEIS);
        log.info("NetActEventManager registered on UEIS: {}", INTERESTING_NETACT_UEIS);
    }

    @Deactivate
    public void deactivate(){
        try {
            eventSubscriptionService.removeEventListener(this, INTERESTING_NETACT_UEIS);
            log.info("NetActEventManager deregistered");
        } catch (Exception e) {
            log.warn("Error while deregistering listener", e);
        }
    }

    @Override
    public String getName() {
        return "netActEventManager";
    }

    @Override
    public int getNumThreads() {
        return 1;
    }

    @Override
    public void onEvent(InMemoryEvent e) {

        log.info("Arrived new event filtered to NetActEventManager: {}", e);

        String hostAddress = e.getInterface().getHostAddress();
        /*Node node = findNodeByIp(hostAddress);
        log.info("node By ip: {}", node);

        if(node == null){
            log.info("The event comes from an unprovisioned node, it is not processed.");
            return;
        }*/

        Optional<Connection> optConnection = connectionManager.getConnection(hostAddress);
        if(optConnection.isEmpty()){
            log.info("No connection configured for IP {}, event is not processed.", hostAddress);
            return;
        }
        Connection connection = optConnection.get();
        //dominio di appartenenza recuperata dal nodo o dalla connection NA8/NA17/MANTARAY
        String dominio = connection.getDomain();

        log.info("Found connection for IP {} -> alias={}, domain={}, address={}",
                hostAddress, connection.getAlias(), dominio, connection.getAddress());


        String sequenceId = e.getParametersByName(SEQUENCE_ID_NETACT_PARAMETER).stream()
                .findFirst().map(EventParameter::getValue).orElse(null);
        String objectInstance = e.getParametersByName(OBJ_INSTANCE_PARAMETER).stream()
                .findFirst().map(EventParameter::getValue).orElse(null);
        String eventTime = e.getParametersByName(EVENT_TIME_NETACT_PARAMETER).stream()
                .findFirst().map(EventParameter::getValue).orElse(null);
        String optionalInfo = e.getParametersByName(OPT_INFO_NETACT_PARAMETER).stream()
                .findFirst().map(EventParameter::getValue).orElse(null);

        RequisitionNode nodeFromEvent = getNodeFromEvent(dominio, sequenceId, objectInstance, eventTime, optionalInfo);
        if(nodeFromEvent==null){
            log.info("Unable to create RequisitionNode from event.");
            return;
        }

        log.info("Add {} node in requisition: {}", dominio, nodeFromEvent);
        final var requisition = ImmutableRequisition.newBuilder()
                .setForeignSource(dominio)
                .addNode(nodeFromEvent)
                .build();

    }

    /*private Node findNodeByIp(String ip) {

        for (Node node : nodeDao.getNodes()) {
            boolean hasIp = node.getIpInterfaces().stream()
                    .map(IpInterface::getIpAddress)       // InetAddress
                    .filter(Objects::nonNull)
                    .map(InetAddress::getHostAddress)     // "129.0.30.137"
                    .anyMatch(addr -> addr.equals(ip));

            if (hasIp) {
                log.info("node found: {}", node);
                return node;
            }
        }
        return null;
    }*/

    private RequisitionNode getNodeFromEvent(String dominio, String sequenceId, String objectInstance, String eventTime, String optionalInfo){

        if(objectInstance==null || objectInstance.isEmpty()){
            log.error("The parameter nbiObjectInstance is null or empty");
            return null;
        }

        String foreignId = objectInstance.trim().replace(SLASH_DELIMITER, UNDERSCORE_DELIMITER);
        String parentForeignId = toParentForeignId(foreignId);
        String nodeLabel = getNodeLabel(optionalInfo, objectInstance);
        String dn ="";
        String tipoApparato = getTipoApparato(optionalInfo);
        String codiceApparato = lastValueOfFDN(objectInstance);
        String codiceSito = getCodiceSito(nodeLabel ,optionalInfo);
        String nomeSito = extractValueFromOptInfo(optionalInfo, "siteObjAddress");
        String ceiCi = extractValueFromOptInfo(optionalInfo, "maintenanceRegion");

        ImmutableRequisitionNode.Builder builder = ImmutableRequisitionNode.newBuilder()
                .setNodeLabel(nodeLabel)
                .setForeignId(foreignId)
                .addCategory(dominio)
                .addMetaData(ImmutableRequisitionMetaData.newBuilder()
                        .setContext(dominio)
                        .setKey("Foreign Source")
                        .setValue(dominio)
                        .build())
                .addMetaData(ImmutableRequisitionMetaData.newBuilder()
                        .setContext(dominio)
                        .setKey("Parent Foreign ID")
                        .setValue(parentForeignId)
                        .build())
                .addMetaData(ImmutableRequisitionMetaData.newBuilder()
                        .setContext(dominio)
                        .setKey("Dominio")
                        .setValue(dominio)
                        .build())
                .addMetaData(ImmutableRequisitionMetaData.newBuilder()
                        .setContext(dominio)
                        .setKey("DN")
                        .setValue(dn)
                        .build())
                .addMetaData(ImmutableRequisitionMetaData.newBuilder()
                        .setContext(dominio)
                        .setKey("Codice Apparato")
                        .setValue(codiceApparato)
                        .build())
                .addMetaData(ImmutableRequisitionMetaData.newBuilder()
                        .setContext(dominio)
                        .setKey("Nome Apparato")
                        .setValue(nodeLabel)
                        .build());

        if(tipoApparato!=null && !tipoApparato.isEmpty()){
            builder.addMetaData(ImmutableRequisitionMetaData.newBuilder()
                    .setContext(dominio)
                    .setKey("Tipo Apparato")
                    .setValue(tipoApparato)
                    .build());
        }
        if(codiceSito!=null && !codiceSito.isEmpty()){
            builder.addMetaData(ImmutableRequisitionMetaData.newBuilder()
                    .setContext(dominio)
                    .setKey("Codice Sito")
                    .setValue(codiceSito)
                    .build());
        }
        if(nomeSito!=null && !nomeSito.isEmpty()){
            builder.addMetaData(ImmutableRequisitionMetaData.newBuilder()
                    .setContext(dominio)
                    .setKey("Nome Sito")
                    .setValue(nomeSito)
                    .build());
        }
        if(ceiCi!=null && !ceiCi.isEmpty()){
            builder.addMetaData(ImmutableRequisitionMetaData.newBuilder()
                    .setContext(dominio)
                    .setKey("CEI/CI")
                    .setValue(ceiCi)
                    .build());
        }

        return builder.build();
    }

    private String toParentForeignId(String foreignId){

        int lastDelimiter = foreignId.lastIndexOf(UNDERSCORE_DELIMITER);
        if(lastDelimiter<=0){
            return null;
        }

        return foreignId.substring(0,lastDelimiter);
    }

    private String getNodeLabel(String optionalInfo, String objectInstance){

        if(optionalInfo!=null && !optionalInfo.isEmpty()){

            String NENameValue = extractValueFromOptInfo(optionalInfo, "NEName");
            if(NENameValue != null ){
                return NENameValue;
            }
        }
        return lastValueOfFDN(objectInstance);
    }

    private String lastValueOfFDN(String objectInstance){

        int lastSlash = objectInstance.lastIndexOf(SLASH_DELIMITER);
        if (lastSlash < 0) {
            return objectInstance;
        }

        // Ritorno l'ultimo livello
        return objectInstance.substring(lastSlash + 1);
    }

    private String extractValueFromOptInfo(String optionalInformation, String key) {

        String completeKey = key + "="; //"key=" ad esempio ->"NEName="
        // Split per "|"
        String[] parts = optionalInformation.split("|");

        for (String part : parts) {
            if (part.startsWith(completeKey)) {
                // Prendo ciò che viene dopo "key="
                String value = part.substring(completeKey.length()).trim();
                // Se vuoto → non valido
                if (value.isEmpty()) return null;

                return value;
            }
        }
        // Attributo non trovato
        return null;
    }
    
    private String getTipoApparato(String optionalInformation) {
        
        String vendorKey = "vendor";
        String versionKey = "version";

        String vendorValue = extractValueFromOptInfo(optionalInformation, vendorKey);
        String versionValue = extractValueFromOptInfo(optionalInformation, versionKey);

        if(vendorValue!=null && versionValue!=null){
            return vendorValue+versionValue;
        }
        return null;
    }

    private String getCodiceSito(String nodeLabel, String optionalInfo){

        if( nodeLabel!= null && nodeLabel.startsWith("L")){
            return nodeLabel.substring(0,8);
        }

        return extractValueFromOptInfo(optionalInfo, "siteObjName");

    }

}
