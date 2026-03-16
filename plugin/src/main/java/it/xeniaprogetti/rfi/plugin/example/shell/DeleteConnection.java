package it.xeniaprogetti.rfi.plugin.example.shell;

import it.xeniaprogetti.rfi.plugin.example.connection.Connection;
import it.xeniaprogetti.rfi.plugin.example.connection.ConnectionManager;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.snmp4j.mp.SnmpConstants;

import java.util.Optional;

@Command(scope = "opennms-rfi-plugin-example", name = "connection-delete", description = "Delete a connection", detailedDescription = "Delete credential for SNMP connection")
@Service
public class DeleteConnection implements Action {

    @Reference
    private ConnectionManager connectionManager;

    @Argument(name = "alias", description = "Alias of the snmp connection credential", required = true)
    public String alias = null;

    @Override
    public Object execute() throws Exception {


        if(connectionManager.deleteConnection(alias)){

            System.out.println("Snmp connection parameters deleted for alias: " + alias);
            return null;

        } else {
            System.err.println("Delete snmp connection failed for alias: " + alias);
        }
        return null;
    }
}
