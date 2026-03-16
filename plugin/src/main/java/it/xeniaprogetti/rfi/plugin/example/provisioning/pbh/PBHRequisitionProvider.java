package it.xeniaprogetti.rfi.plugin.example.provisioning.pbh;

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

@Component(name = "pbhRequisitionProvider",
        immediate = true,
        property = { "type=PBH" },
        service = RequisitionProvider.class)
public class PBHRequisitionProvider extends AbstractRequisitionProvider<PBHNode> /*implements RequisitionProvider*/ {

    private static final Logger LOG = LoggerFactory.getLogger(PBHRequisitionProvider.class);
    private final static String TYPE = "PBH";

    public PBHRequisitionProvider() {

    }

    @Activate
    public void activate() {
        LOG.info("PBHRequisitionProvider activated successfully!");
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /*@Override
    protected List<PBHNode> readFile(String csvPath) {
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

                // split sul separatore ";" tenendo conto delle virgolette
                String[] parts = parseCsvLineWithQuotes(line);

                if (parts.length != 9) {
                    LOG.warn("Skipping invalid pbh entry (expected 9 columns, got {}): {}", parts.length, line);
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
    }*/

    @Override
    protected List<PBHNode> readFile(String csvPath) {

        LOG.info("start read PBH file from path: {}", csvPath);

        List<PBHNode> list = new ArrayList<>();

        for (CSVRecord r : readCsv(csvPath)) {

            String dtp           = r.get(0);
            String nomeImpianto  = r.get(1);
            String tipoImpianto  = r.get(2);
            String matricola     = r.get(3);
            String sedeImpianto  = r.get(4);
            String comune        = r.get(5);
            String provincia     = r.get(6);
            String iccid         = r.get(7);
            String msisdn        = r.get(8);

            list.add(new PBHNode(
                    dtp, nomeImpianto, tipoImpianto,
                    matricola, sedeImpianto, comune,
                    provincia, iccid, msisdn
            ));
        }

        return list;
    }

    @Override
    protected RequisitionNode getNodeFromEntry(PBHNode pbhNode){

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
                .addMetaData(ImmutableRequisitionMetaData.newBuilder()
                        .setContext(TYPE)
                        .setKey("Parent Foreign ID")
                        .setValue("PBHOC")
                        .build())
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
