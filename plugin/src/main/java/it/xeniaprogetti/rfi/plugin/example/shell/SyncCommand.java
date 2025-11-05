package it.xeniaprogetti.rfi.plugin.example.shell;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import it.xeniaprogetti.rfi.plugin.example.AlarmForwarder;
import it.xeniaprogetti.rfi.plugin.example.clients.SnmpClient;
import it.xeniaprogetti.rfi.plugin.example.connection.Connection;
import it.xeniaprogetti.rfi.plugin.example.connection.ConnectionManager;
import it.xeniaprogetti.rfi.plugin.example.snmp.AdvancedSnmpSet;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Command(scope = "opennms-rfi-plugin-example", name = "sync", description = "Start syncronization snmp.")
@Service
public class SyncCommand implements Action {

    @Reference
    ConnectionManager connectionManager;

    @Reference
    SnmpClient snmpClient;

    @Argument(name = "alias", description = "Alias of the snmp connection credential", required = true)
    public String alias = null;

    @Override
    public Object execute() throws IOException {

        Optional<Connection> connection = connectionManager.getConnection(alias);

        if(connection.isEmpty()){
            System.err.println("Connection not found for alias: " + alias);
        }

        AdvancedSnmpSet setterClient = snmpClient.getSnmpClient(connection.get());

        Map<String, Variable> multipleValues = new HashMap<>();
        multipleValues.put("1.3.6.1.2.1.1.4.0", new OctetString("admin@company.com"));
        multipleValues.put("1.3.6.1.2.1.1.6.0", new OctetString("Server Room"));
        multipleValues.put("1.3.6.1.2.1.1.7.0", new Integer32(72));

        setterClient.setMultiple(multipleValues);

        return null;
    }
}
