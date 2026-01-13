package it.xeniaprogetti.rfi.plugin.example.provisioning.desigo;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.xeniaprogetti.rfi.plugin.example.provisioning.Request;
import it.xeniaprogetti.rfi.plugin.example.provisioning.RequestContext;
import org.opennms.integration.api.v1.config.requisition.Requisition;
import org.opennms.integration.api.v1.config.requisition.RequisitionNode;
import org.opennms.integration.api.v1.config.requisition.immutables.ImmutableRequisition;
import org.opennms.integration.api.v1.config.requisition.immutables.ImmutableRequisitionMetaData;
import org.opennms.integration.api.v1.config.requisition.immutables.ImmutableRequisitionNode;
import org.opennms.integration.api.v1.requisition.RequisitionProvider;
import org.opennms.integration.api.v1.requisition.RequisitionRequest;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component(name = "desigoRequisitionProvider",
        immediate = true)
public class DesigoRequisitionProvider implements RequisitionProvider {

    private static final Logger LOG = LoggerFactory.getLogger(DesigoRequisitionProvider.class);
    private final static String TYPE = "SPvA";
    private final static String PARAMETER_PATH = "path";
    //private static final String METADATA_CONTEXT = "requisition";

    //private NodeDao nodeDao;

    public DesigoRequisitionProvider(){

    }

    /*@Reference
    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }*/

    @Activate
    public void activate() {
        LOG.info("DesigoRequisitionProvider activated successfully!");
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public RequisitionRequest getRequest(Map<String, String> parameters) {
        final var path = Objects.requireNonNull(parameters.get(PARAMETER_PATH), "Missing requisition parameter: path");
        return new Request(path);
    }

    @Override
    public Requisition getRequisition(RequisitionRequest requisitionRequest) {
        final var request = (Request) requisitionRequest;
        return this.handleRequest(new RequestContext(request));
    }

    @Override
    public byte[] marshalRequest(RequisitionRequest request) {
        //return new byte[0];
        final var mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsBytes(request);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RequisitionRequest unmarshalRequest(byte[] bytes) {
        //return null;
        final var mapper = new ObjectMapper();
        try {
            return mapper.readValue(bytes, RequisitionRequest.class);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Requisition handleRequest(final RequestContext context){

        LOG.info("start handle request to read file and add Desigo nodes");

        final var requisition = ImmutableRequisition.newBuilder()
                .setForeignSource(TYPE);

        //leggi il file dal path e crea lista di nodi da scorrere per creare RequisitionNode
        List<DesigoNode> desigoNodeList = readFile(context.getPath());

        for(DesigoNode desigoNode: desigoNodeList){
            LOG.debug("Add desigo node in requisition: {}", desigoNode);
            requisition.addNode(getNodeFromEntry(desigoNode, context));
        }

        return requisition.build();
    }

    private List<DesigoNode> readFile(String csvPath) {
        LOG.info("start read Desigo file from path: {}", csvPath);
        List<DesigoNode> desigoNodeList = new ArrayList<>();

        Path path = Paths.get(csvPath);

        if (!Files.exists(path)) {
            LOG.error("Desigo CSV file not found: {}", csvPath);
            throw new IllegalArgumentException("Desigo CSV file not found: " + csvPath);
        }

        if (!Files.isReadable(path)) {
            LOG.error("Desigo CSV file not readable: {}", csvPath);
            throw new IllegalStateException("Desigo CSV file not readable: " + csvPath);
        }

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {

            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                // evita righe vuote
                if (line.isBlank()) {
                    continue;
                }

                // skip header (Code;Name)
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                // split sul separatore ";"
                String[] parts = line.split(";", -1);
                if (parts.length < 2) {
                    LOG.warn("Skipping invalid line: {}", line);
                    continue;
                }

                String code = parts[0].trim();
                String name = parts[1].trim();

                desigoNodeList.add(new DesigoNode(code, name));
            }

        } catch (IOException e) {
            LOG.error("Error reading Desigo CSV file from path:{}", csvPath, e);
            throw new RuntimeException("Error reading Desigo CSV file", e);
        }

        return desigoNodeList;
    }


    private RequisitionNode getNodeFromEntry(DesigoNode desigoNode, RequestContext context){

        ImmutableRequisitionNode.Builder builder = ImmutableRequisitionNode.newBuilder()
                .setNodeLabel(desigoNode.getCode())
                .setForeignId(desigoNode.getCode())
                .addCategory(TYPE)
                .addMetaData(ImmutableRequisitionMetaData.newBuilder()
                        .setContext(TYPE)
                        .setKey("Dominio")
                        .setValue(TYPE)
                        .build())
                .addMetaData(ImmutableRequisitionMetaData.newBuilder()
                        .setContext(TYPE)
                        .setKey("Sistema")
                        .setValue("Desigo")
                        .build())
                .addMetaData(ImmutableRequisitionMetaData.newBuilder()
                        .setContext(TYPE)
                        .setKey("Foreign Source")
                        .setValue("SPvA")
                        .build())
                /*.addMetaData(ImmutableRequisitionMetaData.newBuilder()
                        .setContext(TYPE)
                        .setKey("ParentForeignID")
                        .setValue("SERVER_DESIGO")
                        .build())*/
                .addMetaData(ImmutableRequisitionMetaData.newBuilder()
                        .setContext(TYPE)
                        .setKey("Codice Apparato")
                        .setValue(desigoNode.getCode())
                        .build())
                .addMetaData(ImmutableRequisitionMetaData.newBuilder()
                        .setContext(TYPE)
                        .setKey("Nome Apparato")
                        .setValue(desigoNode.getName())
                        .build());

        ImmutableRequisitionNode requisitionNode = builder.build();
        LOG.info("Requisition node created: {}", requisitionNode);
        return requisitionNode;
    }

    /*public static class Request implements RequisitionRequest {

        private String path;

        public Request(String path) {
            this.path = Objects.requireNonNull(path);
        }
    }

    public static class RequestContext {
        private final Request request;

        public RequestContext(final Request request) {
            this.request = Objects.requireNonNull(request);
        }

        public Request getRequest() {
            return this.request;
        }

        public String getPath() {
            return this.request.path;
        }
    }*/
}
