package it.xeniaprogetti.rfi.plugin.example.snmp;

import it.xeniaprogetti.rfi.plugin.example.clients.SnmpCredentials;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdvancedSnmpGet {

    private Snmp snmp;
    private CommunityTarget target;
    private int version;

    public AdvancedSnmpGet(final SnmpCredentials credentials) throws IOException {
        TransportMapping<?> transport = new DefaultUdpTransportMapping();
        snmp = new Snmp(transport);
        transport.listen();

        this.version = credentials.getVersion();

        target = new CommunityTarget();
        target.setAddress(GenericAddress.parse(credentials.getAddress()));
        target.setCommunity(new OctetString(credentials.getCommunity()));
        target.setVersion(this.version);
        target.setTimeout(1500);
        target.setRetries(2);
    }

    /**
     * GET a single OID.
     * @return the Variable value, or null if not found or on error
     */
    public Variable get(String oid) {
        Map<String, Variable> result = getMultiple(List.of(oid));
        return result.get(oid);
    }

    /**
     * GET multiple OIDs in a single request.
     * @return map of OID string to Variable; absent entries indicate no response for that OID
     */
    public Map<String, Variable> getMultiple(List<String> oids) {
        try {
            PDU pdu = new PDU();
            pdu.setType(PDU.GET);
            for (String oid : oids) {
                pdu.add(new VariableBinding(new OID(oid)));
            }

            ResponseEvent responseEvent = snmp.get(pdu, target);
            if (responseEvent != null && responseEvent.getResponse() != null) {
                PDU response = responseEvent.getResponse();
                if (response.getErrorStatus() == PDU.noError) {
                    Map<String, Variable> result = new HashMap<>();
                    for (VariableBinding vb : response.getVariableBindings()) {
                        result.put(vb.getOid().toString(), vb.getVariable());
                    }
                    return result;
                }
            }
        } catch (IOException e) {
            System.err.println("GET error: " + e.getMessage());
        }
        return Collections.emptyMap();
    }

    /**
     * Walk a subtree starting at the given OID.
     * Uses GETBULK for SNMPv2c, GETNEXT for SNMPv1.
     * Continues issuing requests until the returned OIDs leave the subtree.
     *
     * @param rootOid         the base OID of the subtree to walk
     * @param maxRepetitions  max-repetitions per GETBULK PDU (ignored for SNMPv1)
     * @return ordered list of VariableBindings for the entire subtree
     */
    public List<VariableBinding> walk(String rootOid, int maxRepetitions) {
        if (version == SnmpConstants.version2c) {
            return walkBulk(rootOid, maxRepetitions);
        }
        return walkNext(rootOid);
    }

    /**
     * Convenience walk with default max-repetitions of 10.
     */
    public List<VariableBinding> walk(String rootOid) {
        return walk(rootOid, 10);
    }

    // SNMPv2c walk via GETBULK
    @SuppressWarnings("unchecked")
    private List<VariableBinding> walkBulk(String rootOid, int maxRepetitions) {
        List<VariableBinding> result = new ArrayList<>();
        OID root = new OID(rootOid);
        OID nextOid = root;

        try {
            while (true) {
                PDU pdu = new PDU();
                pdu.setType(PDU.GETBULK);
                pdu.setNonRepeaters(0);
                pdu.setMaxRepetitions(maxRepetitions);
                pdu.add(new VariableBinding(nextOid));

                ResponseEvent<?> responseEvent = snmp.send(pdu, target);
                if (responseEvent == null || responseEvent.getResponse() == null) {
                    break;
                }

                PDU response = responseEvent.getResponse();
                if (response.getErrorStatus() != PDU.noError) {
                    break;
                }

                List<? extends VariableBinding> vbs = response.getVariableBindings();
                if (vbs == null || vbs.isEmpty()) {
                    break;
                }

                for (VariableBinding vb : vbs) {
                    OID oid = vb.getOid();
                    if (!oid.startsWith(root) || vb.isException()) {
                        return result;
                    }
                    result.add(vb);
                    nextOid = oid;
                }
            }
        } catch (IOException e) {
            System.err.println("GETBULK walk error: " + e.getMessage());
        }
        return result;
    }

    // SNMPv1 walk via GETNEXT
    @SuppressWarnings("unchecked")
    private List<VariableBinding> walkNext(String rootOid) {
        List<VariableBinding> result = new ArrayList<>();
        OID root = new OID(rootOid);
        OID nextOid = root;

        try {
            while (true) {
                PDU pdu = new PDU();
                pdu.setType(PDU.GETNEXT);
                pdu.add(new VariableBinding(nextOid));

                ResponseEvent<?> responseEvent = snmp.getNext(pdu, target);
                if (responseEvent == null || responseEvent.getResponse() == null) {
                    break;
                }

                PDU response = responseEvent.getResponse();
                if (response.getErrorStatus() != PDU.noError) {
                    break;
                }

                List<? extends VariableBinding> vbs = response.getVariableBindings();
                if (vbs == null || vbs.isEmpty()) {
                    break;
                }

                VariableBinding vb = vbs.get(0);
                OID oid = vb.getOid();
                if (!oid.startsWith(root) || vb.isException()) {
                    break;
                }
                result.add(vb);
                nextOid = oid;
            }
        } catch (IOException e) {
            System.err.println("GETNEXT walk error: " + e.getMessage());
        }
        return result;
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