package it.xeniaprogetti.rfi.plugin.example.provisioning.pbh;

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

@Component(name = "pbhRequisitionProvider",
        immediate = true)
public class PBHRequisitionProvider implements RequisitionProvider {

    private static final Logger LOG = LoggerFactory.getLogger(PBHRequisitionProvider.class);
    private final static String TYPE = "PBH";
    private final static String PARAMETER_PATH = "path";

    //private NodeDao nodeDao;

    public PBHRequisitionProvider() {

    }

    /*@Reference
    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }*/

    @Activate
    public void activate() {
        LOG.info("PBHRequisitionProvider activated successfully!");
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

    private Requisition handleRequest(final RequestContext context) {

        LOG.info("start handle request to read file and add PBH nodes");

        final var requisition = ImmutableRequisition.newBuilder()
                .setForeignSource(TYPE);

        List<PBHNode> pbhNodeList = readFile(context.getPath());
        for (PBHNode pbhNode : pbhNodeList) {
            LOG.debug("Add PBH node in requisition: {}", pbhNode);
            requisition.addNode(getNodeFromEntry(pbhNode, context));
        }

        return requisition.build();
    }

    private List<PBHNode> readFile(String csvPath) {
        LOG.info("start read PBH file from path: {}", csvPath);
        List<PBHNode> pbhNodeList = new ArrayList<>();

        Path path = Paths.get(csvPath);

        if (!Files.exists(path)) {
            LOG.error("PBH CSV file not found: {}", csvPath);
            throw new IllegalArgumentException("PBH CSV file not found: " + csvPath);
        }

        if (!Files.isReadable(path)) {
            LOG.error("PBH CSV file not readable: {}", csvPath);
            throw new IllegalStateException("PBH CSV file not readable: " + csvPath);
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
                if (parts.length < 9) {
                    LOG.warn("Skipping invalid line: {}", line);
                    continue;
                }

                String dtp = parts[0].trim();
                String nomeImpianto = parts[1].trim();
                String tipoImpianto = parts[2].trim();
                String matricola = parts[3].trim();
                String sedeImpianto = parts[4].trim();
                String comune = parts[5].trim();
                String provincia = parts[6].trim();
                String iccid = parts[7].trim();
                String msisdn = parts[8].trim();


                pbhNodeList.add(new PBHNode(dtp, nomeImpianto, tipoImpianto, matricola, sedeImpianto, comune, provincia, iccid, msisdn));
            }

        } catch (IOException e) {
            LOG.error("Error reading PBH CSV file from path:{}", csvPath, e);
            throw new RuntimeException("Error reading PBH CSV file", e);
        }

        return pbhNodeList;
    }

    private RequisitionNode getNodeFromEntry(PBHNode pbhNode, RequestContext context){

        String nomeSito = pbhNode.getComune() + " - " + pbhNode.getProvincia();

        ImmutableRequisitionNode.Builder builder = ImmutableRequisitionNode.newBuilder()
                .setNodeLabel(pbhNode.getMsisdn())
                .setForeignId(pbhNode.getMsisdn())
                .addCategory(TYPE)
                .addMetaData(ImmutableRequisitionMetaData.newBuilder()
                        .setContext(TYPE)
                        .setKey("Dominio")
                        .setValue(TYPE)
                        .build())
                .addMetaData(ImmutableRequisitionMetaData.newBuilder()
                        .setContext(TYPE)
                        .setKey("Sistema")
                        .setValue("SC PLP")
                        .build())
                .addMetaData(ImmutableRequisitionMetaData.newBuilder()
                        .setContext(TYPE)
                        .setKey("Foreign Source")
                        .setValue(TYPE)
                        .build())
                /*.addMetaData(ImmutableRequisitionMetaData.newBuilder()
                        .setContext(TYPE)
                        .setKey("Parent Foreign ID")
                        .setValue("PBHOC")
                        .build())*/
                .addMetaData(ImmutableRequisitionMetaData.newBuilder()
                        .setContext(TYPE)
                        .setKey("Tipo Apparato")
                        .setValue(pbhNode.getNomeImpianto())
                        .build())
                .addMetaData(ImmutableRequisitionMetaData.newBuilder()
                        .setContext(TYPE)
                        .setKey("Codice Apparato")
                        .setValue(pbhNode.getMsisdn())
                        .build())
                .addMetaData(ImmutableRequisitionMetaData.newBuilder()
                        .setContext(TYPE)
                        .setKey("Nome Apparato")
                        .setValue(pbhNode.getTipoImpianto())
                        .build())
                .addMetaData(ImmutableRequisitionMetaData.newBuilder()
                        .setContext(TYPE)
                        .setKey("Codice Sito")
                        .setValue(pbhNode.getSedeImpianto())
                        .build())
                .addMetaData(ImmutableRequisitionMetaData.newBuilder()
                        .setContext(TYPE)
                        .setKey("Nome Sito")
                        .setValue(nomeSito)
                        .build())
                .addMetaData(ImmutableRequisitionMetaData.newBuilder()
                        .setContext(TYPE)
                        .setKey("CEI/CI")
                        .setValue(pbhNode.getDtp())
                        .build())
                .addMetaData(ImmutableRequisitionMetaData.newBuilder()
                        .setContext(TYPE)
                        .setKey("Tratta")
                        .setValue(pbhNode.getTipoImpianto())
                        .build())
                .addMetaData(ImmutableRequisitionMetaData.newBuilder()
                        .setContext(TYPE)
                        .setKey("DOIT")
                        .setValue(pbhNode.getDtp())
                        .build())
                .addMetaData(ImmutableRequisitionMetaData.newBuilder()
                        .setContext(TYPE)
                        .setKey("UT")
                        .setValue(pbhNode.getDtp())
                        .build());

        ImmutableRequisitionNode requisitionNode = builder.build();
        LOG.info("Requisition node created: {}", requisitionNode);
        return requisitionNode;
    }
}
