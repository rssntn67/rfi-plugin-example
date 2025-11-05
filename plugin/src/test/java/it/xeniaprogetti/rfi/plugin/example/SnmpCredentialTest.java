package it.xeniaprogetti.rfi.plugin.example;

import it.xeniaprogetti.rfi.plugin.example.clients.SnmpCredentials;
import it.xeniaprogetti.rfi.plugin.example.snmp.AdvancedSnmpSet;
import org.junit.Test;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

import java.util.HashMap;
import java.util.Map;

public class SnmpCredentialTest {


    /**
     * Verifies that the object is serialized to JSON as expected.
     */
    @Test
    public void testCredential()  {

        SnmpCredentials snmpCredentials = SnmpCredentials.builder()
                .withAddress("udp://192.168.1.1/161")
                .withCommunity("private")
                .withVersion(SnmpConstants.version2c)
                .build();
    }
}
