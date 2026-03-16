package it.xeniaprogetti.rfi.plugin.example;

import it.xeniaprogetti.rfi.plugin.example.clients.SnmpCredentials;
import it.xeniaprogetti.rfi.plugin.example.snmp.AdvancedSnmpGet;
import org.junit.Test;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

import java.util.List;
import java.util.Map;

public class SnmpGetTest {

    // SNMP system table OIDs (RFC 1213 / MIB-II)
    private static final String SYS_DESCR    = "1.3.6.1.2.1.1.1.0";
    private static final String SYS_UPTIME   = "1.3.6.1.2.1.1.3.0";
    private static final String SYS_CONTACT  = "1.3.6.1.2.1.1.4.0";
    private static final String SYS_NAME     = "1.3.6.1.2.1.1.5.0";
    private static final String SYS_LOCATION = "1.3.6.1.2.1.1.6.0";

    // Root OID of the system subtree
    private static final String SYS_TABLE = "1.3.6.1.2.1.1";

    @Test
    public void canGetSystemScalarsV1() {
        AdvancedSnmpGet snmpGet = null;
        try {
            snmpGet = new AdvancedSnmpGet(SnmpCredentials.builder()
                    .withAddress("udp:192.168.1.1/161")
                    .withCommunity("public")
                    .withVersion(SnmpConstants.version1)
                    .withIpAddr("192.168.1.1")
                    .build());

            Map<String, Variable> result = snmpGet.getMultiple(List.of(
                    SYS_DESCR, SYS_CONTACT, SYS_NAME, SYS_LOCATION, SYS_UPTIME
            ));

            System.out.println("=== SNMP GET system scalars (v1) ===");
            System.out.println("sysDescr    : " + result.get(SYS_DESCR));
            System.out.println("sysContact  : " + result.get(SYS_CONTACT));
            System.out.println("sysName     : " + result.get(SYS_NAME));
            System.out.println("sysLocation : " + result.get(SYS_LOCATION));
            System.out.println("sysUpTime   : " + result.get(SYS_UPTIME));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (snmpGet != null) snmpGet.close();
        }
    }

    @Test
    public void canGetSystemScalarsV2c() {
        AdvancedSnmpGet snmpGet = null;
        try {
            snmpGet = new AdvancedSnmpGet(SnmpCredentials.builder()
                    .withAddress("udp:192.168.1.1/161")
                    .withCommunity("public")
                    .withVersion(SnmpConstants.version2c)
                    .withIpAddr("192.168.1.1")
                    .build());

            Map<String, Variable> result = snmpGet.getMultiple(List.of(
                    SYS_DESCR, SYS_CONTACT, SYS_NAME, SYS_LOCATION, SYS_UPTIME
            ));

            System.out.println("=== SNMP GET system scalars (v2c) ===");
            System.out.println("sysDescr    : " + result.get(SYS_DESCR));
            System.out.println("sysContact  : " + result.get(SYS_CONTACT));
            System.out.println("sysName     : " + result.get(SYS_NAME));
            System.out.println("sysLocation : " + result.get(SYS_LOCATION));
            System.out.println("sysUpTime   : " + result.get(SYS_UPTIME));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (snmpGet != null) snmpGet.close();
        }
    }

    @Test
    public void canWalkSystemTableV1() {
        AdvancedSnmpGet snmpGet = null;
        try {
            snmpGet = new AdvancedSnmpGet(SnmpCredentials.builder()
                    .withAddress("udp:192.168.1.1/161")
                    .withCommunity("public")
                    .withVersion(SnmpConstants.version1)
                    .withIpAddr("192.168.1.1")
                    .build());

            // SNMPv1: walk uses GETNEXT internally
            List<VariableBinding> vbs = snmpGet.walk(SYS_TABLE);

            System.out.println("=== SNMP GETNEXT walk of system table (v1) ===");
            for (VariableBinding vb : vbs) {
                System.out.println(vb.getOid() + " = " + vb.getVariable());
            }
            System.out.println("Total entries: " + vbs.size());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (snmpGet != null) snmpGet.close();
        }
    }

    @Test
    public void canWalkSystemTableV2c() {
        AdvancedSnmpGet snmpGet = null;
        try {
            snmpGet = new AdvancedSnmpGet(SnmpCredentials.builder()
                    .withAddress("udp:192.168.1.1/161")
                    .withCommunity("public")
                    .withVersion(SnmpConstants.version2c)
                    .withIpAddr("192.168.1.1")
                    .build());

            // SNMPv2c: walk uses GETBULK internally (maxRepetitions=10 by default)
            List<VariableBinding> vbs = snmpGet.walk(SYS_TABLE);

            System.out.println("=== SNMP GETBULK walk of system table (v2c) ===");
            for (VariableBinding vb : vbs) {
                System.out.println(vb.getOid() + " = " + vb.getVariable());
            }
            System.out.println("Total entries: " + vbs.size());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (snmpGet != null) snmpGet.close();
        }
    }
}