package it.xeniaprogetti.rfi.plugin.example.shell;

import it.xeniaprogetti.rfi.plugin.example.connection.ConnectionManager;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.table.Col;
import org.apache.karaf.shell.support.table.ShellTable;

@Command(scope = "opennms-rfi-plugin-example", name = "connection-list", description = "List all connections", detailedDescription = "Delete credential for SNMP connection")
@Service
public class ListConnection implements Action {

    @Reference
    private ConnectionManager connectionManager;

    @Reference
    private Session session;

    @Argument(name = "alias", description = "Alias of the snmp connection credential", required = true)
    public String alias = null;

    @Override
    public Object execute() throws Exception {

        final var table = new ShellTable()
                .size(session.getTerminal().getWidth()-1)
                .column(new Col("Alias").maxSize(36))
                .column(new Col("Address").maxSize(50))
                .column(new Col("Community"). maxSize(36))
                .column(new Col("Version").maxSize(10));

        connectionManager.getAliases().stream()
                .map(alias ->
                        this.connectionManager
                                .getConnection(alias)
                                .orElseThrow()).forEach(connection -> {
                    final var row = table.addRow();
                    row.addContent(connection.getAlias());
                    row.addContent(connection.getAddress());
                    row.addContent(connection.getCommunity());
                    row.addContent(connection.getVersion());


                });

        table.print(System.out, true);

        return null;
    }
}
