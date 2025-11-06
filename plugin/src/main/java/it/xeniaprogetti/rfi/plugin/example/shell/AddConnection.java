package it.xeniaprogetti.rfi.plugin.example.shell;

import it.xeniaprogetti.rfi.plugin.example.connection.ConnectionManager;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.snmp4j.mp.SnmpConstants;

@Command(scope = "opennms-rfi-plugin-example", name = "connection-add", description = "Add a connection",
        detailedDescription = "Add credential for SNMP connection")
@Service
public class AddConnection implements Action {

    @Reference
    private ConnectionManager connectionManager;

    @Argument(name = "alias", description = "Alias of the snmp connection credential", required = true)
    public String alias = null;

    @Argument(index = 1, name = "address", description = "Address in the form: udp|tcp:<ip>/<port>", required = true)
    public String address = null;

    @Argument(index = 2, name = "community", description = "Community  of the snmp connection credential", required = true, censor = true)
    public String community = null;

    @Argument(index = 3, name = "version",
            description = "Version of the snmp connection credential. Version allowed: {version1=0, version2c=1, version3=3}",
            required = false)
    public int version = SnmpConstants.version2c;

    @Override
    public Object execute() throws Exception {

        if(connectionManager.getConnection(alias).isPresent()){
            System.err.println("Alias already exist: "+ alias);
            return null;
        }

        final var connection = this.connectionManager.newConnection(address, community, version, alias);

        connection.save();

        System.out.println("Connection info saved for alias: " + alias);

        return null;
    }
}
