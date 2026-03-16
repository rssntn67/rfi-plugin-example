package it.xeniaprogetti.rfi.plugin.example.webhook;

import javax.ws.rs.core.Response;

import it.xeniaprogetti.rfi.plugin.example.SyncService;
import it.xeniaprogetti.rfi.plugin.example.connection.Connection;
import it.xeniaprogetti.rfi.plugin.example.connection.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class WebhookHandlerImpl implements WebhookHandler {
    private static final Logger LOG = LoggerFactory.getLogger(WebhookHandlerImpl.class);

    private final SyncService syncService;
    private final ConnectionManager connectionManager;

    public WebhookHandlerImpl(SyncService syncService, ConnectionManager connectionManager) {
        this.syncService = syncService;
        this.connectionManager = connectionManager;
    }

    @Override
    public Response ping() {
        return Response.ok("pong").build();
    }

    @Override
    public Response handleWebhook(String body) {
        LOG.debug("Got payload: {}", body);
        return Response.ok().build();
    }

    @Override
    public Response sync(String alias) {
        if(syncService.sync(alias) ){
            return Response.ok().build();
        }

        return Response.status(Response.Status.BAD_REQUEST).entity("sync failed").build();
    }

    @Override
    public List<ConnectionDto> getConnectionList() {

        final List<ConnectionDto> connections = new ArrayList<>();

        connectionManager.getAliases().stream()
                .map(alias ->
                        this.connectionManager
                                .getConnection(alias)
                                .orElseThrow()).forEach(connection -> {

                            connections.add(getConnectionDto(connection));
                        }
                );

        return connections;
    }

    @Override
    public ConnectionDto getConnection(String alias) {

        var connection = this.connectionManager.getConnection(alias);

        if(connection.isEmpty()){
            System.err.println("Alias not exist: "+ alias);
            return null;
        }

        return  getConnectionDto(connection.get());
    }

    @Override
    public Response addConnection(ConnectionDto connectionDto) {

        if(connectionManager.getConnection(connectionDto.getAlias()).isPresent()){

            return Response.status(Response.Status.NOT_ACCEPTABLE)
                    .entity("Alias already exist: "+ connectionDto.getAlias()).build();
        }

        final var connection = this.connectionManager.newConnection(
                connectionDto.getAddress(),
                connectionDto.getCommunity(),
                connectionDto.getVersion(),
                connectionDto.getAlias(),
                connectionDto.getDomain());

        connection.save();

        return Response.ok().build();
    }

    @Override
    public Response updateConnection(String alias, ConnectionDto connectionDto) {
        Optional<Connection> connection = connectionManager.getConnection(alias);
        if(connection.isEmpty()){
            return Response.status(Response.Status.NOT_ACCEPTABLE)
                    .entity("Alias not exist: "+ connectionDto.getAlias()).build();
        }

        connection.get().setAddress(connectionDto.getAddress());
        connection.get().setVersion(connectionDto.getVersion());
        if(connectionDto.getCommunity() != null && !connectionDto.getCommunity().isEmpty())
            connection.get().setCommunity(connectionDto.getCommunity());

        connection.get().save();

        return Response.ok().build();
    }

    @Override
    public Response deleteConnection(String alias) {

        if(connectionManager.deleteConnection(alias)){
            return Response.ok().build();
        }

        return Response.status(Response.Status.NOT_ACCEPTABLE)
                .entity("connection delete failed for Alias : "+ alias).build();
    }


    private ConnectionDto getConnectionDto(Connection connection){
        ConnectionDto dto = new ConnectionDto();
        dto.setAlias(connection.getAlias());
        dto.setAddress(connection.getAddress());
        dto.setCommunity(connection.getCommunity());
        dto.setVersion(connection.getVersion());
        return dto;
    }
}

