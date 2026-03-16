package it.xeniaprogetti.rfi.plugin.example.snmp;

import it.xeniaprogetti.rfi.plugin.example.clients.SnmpCredentials;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Counter32;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class AdvancedSnmpSet {

    private Snmp snmp;
    private CommunityTarget target;

    public AdvancedSnmpSet(final SnmpCredentials credentials) throws IOException {
        TransportMapping<?> transport = new DefaultUdpTransportMapping();
        snmp = new Snmp(transport);
        transport.listen();

        target = new CommunityTarget();
        target.setAddress(GenericAddress.parse(credentials.getAddress()));
        target.setCommunity(new OctetString(credentials.getCommunity()));
        target.setVersion(credentials.getVersion());
        target.setTimeout(1500);
        target.setRetries(2);
    }

    /**
     * Set multiple OIDs in a single SET request
     * @param oidValueMap Map of OID to Variable value pairs
     * @return true if all sets were successful, false otherwise
     */
    public boolean setMultiple(Map<String, Variable> oidValueMap) {
        try {
            PDU pdu = new PDU();
            pdu.setType(PDU.SET);

            // Add all variable bindings to the PDU
            for (Map.Entry<String, Variable> entry : oidValueMap.entrySet()) {
                pdu.add(new VariableBinding(new OID(entry.getKey()), entry.getValue()));
            }

            ResponseEvent responseEvent = snmp.set(pdu, target);

            if (responseEvent != null && responseEvent.getResponse() != null) {
                PDU response = responseEvent.getResponse();
                if (response.getErrorStatus() == PDU.noError) {
                    System.out.println("Multiple SET operation successful");
                    return true;
                } else {
                    System.out.println("SET failed: " + response.getErrorStatusText());
                }
            }
        } catch (IOException e) {
            System.err.println("Error during multiple SET: " + e.getMessage());
        }
        return false;
    }

    /**
     * Set a specific OID with type safety
     */
    public boolean setString(String oid, String value) {
        return set(oid, new OctetString(value));
    }

    public boolean setInteger(String oid, int value) {
        return set(oid, new Integer32(value));
    }

    public boolean setCounter(String oid, long value) {
        return set(oid, new Counter32(value));
    }

    private boolean set(String oid, Variable value) {
        try {
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(oid), value));
            pdu.setType(PDU.SET);

            ResponseEvent responseEvent = snmp.set(pdu, target);

            if (responseEvent != null && responseEvent.getResponse() != null) {
                return responseEvent.getResponse().getErrorStatus() == PDU.noError;
            }
        } catch (IOException e) {
            System.err.println("SET error: " + e.getMessage());
        }
        return false;
    }

    public void close() {
        try {
            if (snmp != null) {
                snmp.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing: " + e.getMessage());
        }
    }
}