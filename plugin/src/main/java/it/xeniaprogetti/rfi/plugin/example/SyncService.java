package it.xeniaprogetti.rfi.plugin.example;

import it.xeniaprogetti.rfi.plugin.example.clients.SnmpClient;
import it.xeniaprogetti.rfi.plugin.example.connection.Connection;
import it.xeniaprogetti.rfi.plugin.example.connection.ConnectionManager;
import it.xeniaprogetti.rfi.plugin.example.snmp.AdvancedSnmpSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SyncService {
    private static final Logger LOG = LoggerFactory.getLogger(SyncService.class);

    private final SnmpClient snmpClient;
    private final ConnectionManager connectionManager;

    public SyncService(SnmpClient snmpClient, ConnectionManager connectionManager) {
        this.snmpClient = snmpClient;
        this.connectionManager = connectionManager;
    }

    public boolean sync(String alias){
        Optional<Connection> connection = connectionManager.getConnection(alias);

        if(connection.isEmpty()){
            LOG.error("Connection not found for alias: {}", alias);
            return false;
        }

        try {
            AdvancedSnmpSet setterClient = snmpClient.getSnmpClient(connection.get());
            Map<String, Variable> multipleValues = new HashMap<>();
            multipleValues.put("1.3.6.1.2.1.1.4.0", new OctetString("admin@company.com"));
            multipleValues.put("1.3.6.1.2.1.1.6.0", new OctetString("Server Room"));
            multipleValues.put("1.3.6.1.2.1.1.7.0", new Integer32(72));

            setterClient.setMultiple(multipleValues);

            return true;
        } catch (IOException e) {
            LOG.error("Error on sync : {}", e.getMessage(), e);

            return false;
        }
    }
}
