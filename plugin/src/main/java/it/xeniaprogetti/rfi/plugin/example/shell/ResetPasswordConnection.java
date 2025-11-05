package it.xeniaprogetti.rfi.plugin.example.shell;

import it.xeniaprogetti.rfi.plugin.example.connection.ConnectionManager;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.snmp4j.mp.SnmpConstants;

@Command(scope = "opennms-rfi-plugin-example", name = "connection-reset-password", description = "Reset password a connection", detailedDescription = "Edit credential for SNMP connection")
@Service
public class ResetPasswordConnection implements Action {

    @Reference
    private ConnectionManager connectionManager;

    @Argument(name = "alias", description = "Alias of the snmp connection credential", required = true)
    public String alias = null;

    @Argument(index = 1, name = "community", description = "Community  of the snmp connection credential", required = true, censor = true)
    public String community = null;


    @Override
    public Object execute() throws Exception {

        if(connectionManager.getConnection(alias).isEmpty()){
            System.err.println("Alias not exist: "+ alias);
            return null;
        }

        final var connection = this.connectionManager.getConnection(alias);

        connection.get().setCommunity(community);

        connection.get().save();

        System.out.println("Connection info saved for alias: " + alias);

        return null;
    }
}
