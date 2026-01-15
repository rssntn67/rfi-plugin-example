package it.xeniaprogetti.rfi.plugin.example.provisioning;

import com.fasterxml.jackson.databind.ObjectMapper;
//import org.apache.commons.csv.CSVFormat;
//import org.apache.commons.csv.CSVRecord;
import org.opennms.integration.api.v1.config.requisition.Requisition;
import org.opennms.integration.api.v1.config.requisition.RequisitionNode;
import org.opennms.integration.api.v1.config.requisition.immutables.ImmutableRequisition;
import org.opennms.integration.api.v1.requisition.RequisitionProvider;
import org.opennms.integration.api.v1.requisition.RequisitionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractRequisitionProvider<T> implements RequisitionProvider {

    private final static String PARAMETER_PATH = "path";
    private static final Logger LOG = LoggerFactory.getLogger(AbstractRequisitionProvider.class);

    protected abstract List<T> readFile(String csvPath);
    protected abstract RequisitionNode getNodeFromEntry(T node);

    @Override
    public RequisitionRequest getRequest(Map<String, String> parameters) {
        final var path = Objects.requireNonNull(parameters.get(PARAMETER_PATH), "Missing requisition parameter: path");
        return new Request(path);
    }

    @Override
    public Requisition getRequisition(RequisitionRequest requisitionRequest) {
        final var request = (Request) requisitionRequest;
        return handleRequest(new RequestContext(request));
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

    protected Requisition handleRequest(final RequestContext context){

        LOG.info("start handle request to read file and add {} nodes", getType());

        final var requisition = ImmutableRequisition.newBuilder()
                .setForeignSource(getType());

        //leggi il file dal path e crea lista di nodi da scorrere per creare RequisitionNode
        List<T> nodeList = readFile(context.getPath());

        for(T node: nodeList){
            LOG.debug("Add {} node in requisition: {}", getType(), node);
            requisition.addNode(getNodeFromEntry(node));
        }

        return requisition.build();
    }

    protected String[] parseCsvLineWithQuotes(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                // toggle stato "sono dentro le virgolette"
                inQuotes = !inQuotes;
            } else if (c == ';' && !inQuotes) {
                // separatore di campo valido
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        // ultimo campo
        result.add(current.toString());

        return result.toArray(new String[0]);
    }

    /*protected Iterable<CSVRecord> readCsv(String csvPath) {

        Path path = Paths.get(csvPath);

        if (!Files.exists(path)) {
            throw new IllegalArgumentException("CSV file not found: " + csvPath);
        }

        if (!Files.isReadable(path)) {
            throw new IllegalStateException("CSV file not readable: " + csvPath);
        }

        try {
            Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);

            CSVFormat format = CSVFormat.DEFAULT
                    .builder()
                    .setDelimiter(';')
                    .setTrim(true)
                    .setIgnoreSurroundingSpaces(true)
                    .setQuote('"')
                    .setSkipHeaderRecord(true)
                    .build();

            return format.parse(reader);

        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV file: " + csvPath, e);
        }
    }*/
}
