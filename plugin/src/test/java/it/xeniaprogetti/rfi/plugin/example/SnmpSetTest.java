package it.xeniaprogetti.rfi.plugin.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.xeniaprogetti.rfi.plugin.example.clients.SnmpCredentials;
import it.xeniaprogetti.rfi.plugin.example.model.Alert;
import it.xeniaprogetti.rfi.plugin.example.snmp.AdvancedSnmpSet;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class SnmpSetTest {


    /**
     * Verifies that the object is serialized to JSON as expected.
     */
    @Test
    public void canSetSnmp()  {
        AdvancedSnmpSet snmpSet = null;

        try {

            snmpSet = new AdvancedSnmpSet(SnmpCredentials.builder()
                    .withAddress("udp:192.168.1.1/161")
                    .withCommunity("private")
                    .withVersion(1)
                    .build() );

            // Example 1: Set multiple values at once
            Map<String, Variable> multipleValues = new HashMap<>();
            multipleValues.put("1.3.6.1.2.1.1.4.0", new OctetString("admin@company.com"));
            multipleValues.put("1.3.6.1.2.1.1.6.0", new OctetString("Server Room"));
            multipleValues.put("1.3.6.1.2.1.1.7.0", new Integer32(72));

            boolean success = snmpSet.setMultiple(multipleValues);
            System.out.println("Multiple SET: " + (success ? "Success" : "Failed"));

            // Example 2: Type-safe individual sets
            snmpSet.setString("1.3.6.1.2.1.1.5.0", "MyRouter");
            snmpSet.setInteger("1.3.6.1.2.1.1.7.0", 76);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (snmpSet != null) {
                snmpSet.close();
            }
        }
    }
}
