package it.xeniaprogetti.rfi.plugin.example.provisioning.desigo;

import it.xeniaprogetti.rfi.plugin.example.provisioning.AbstractRequisitionProvider;
import org.apache.commons.csv.CSVRecord;
import org.opennms.integration.api.v1.config.requisition.RequisitionNode;
import org.opennms.integration.api.v1.config.requisition.immutables.ImmutableRequisitionMetaData;
import org.opennms.integration.api.v1.config.requisition.immutables.ImmutableRequisitionNode;
import org.opennms.integration.api.v1.requisition.RequisitionProvider;
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

@Component(name = "desigoRequisitionProvider",
        immediate = true,
        property = { "type=SPvA" },
        service = RequisitionProvider.class)
public class DesigoRequisitionProvider extends AbstractRequisitionProvider<DesigoNode> /*implements RequisitionProvider*/ {

    private static final Logger LOG = LoggerFactory.getLogger(DesigoRequisitionProvider.class);
    private final static String TYPE = "SPvA";

    public DesigoRequisitionProvider(){
    }

    @Activate
    public void activate() {
        LOG.info("DesigoRequisitionProvider activated successfully!");
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /*@Override
    protected List<DesigoNode> readFile(String csvPath) {
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

                String[] parts = parseCsvLineWithQuotes(line);

                if (parts.length != 2) {
                    LOG.warn("Skipping invalid desigo entry (expected 2 columns, got {}): {}", parts.length, line);
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
    }*/

    @Override
    protected List<DesigoNode> readFile(String csvPath) {

        LOG.info("start read PBH file from path: {}", csvPath);

        List<DesigoNode> list = new ArrayList<>();

        for (CSVRecord r : readCsv(csvPath)) {

            String code = r.get(0);
            String name = r.get(1);

            list.add(new DesigoNode(code, name));
        }

        return list;
    }

    @Override
    protected RequisitionNode getNodeFromEntry(DesigoNode node) {
        ImmutableRequisitionNode.Builder builder = ImmutableRequisitionNode.newBuilder()
                .setNodeLabel(node.getCode())
                .setForeignId(node.getCode())
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
                .addMetaData(ImmutableRequisitionMetaData.newBuilder()
                        .setContext(TYPE)
                        .setKey("ParentForeignID")
                        .setValue("SERVER_DESIGO")
                        .build())
                .addMetaData(ImmutableRequisitionMetaData.newBuilder()
                        .setContext(TYPE)
                        .setKey("Codice Apparato")
                        .setValue(node.getCode())
                        .build())
                .addMetaData(ImmutableRequisitionMetaData.newBuilder()
                        .setContext(TYPE)
                        .setKey("Nome Apparato")
                        .setValue(node.getName())
                        .build());

        ImmutableRequisitionNode requisitionNode = builder.build();
        LOG.info("Requisition node created: {}", requisitionNode);
        return requisitionNode;
    }
}
