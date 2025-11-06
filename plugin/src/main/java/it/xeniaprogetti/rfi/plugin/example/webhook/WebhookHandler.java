package it.xeniaprogetti.rfi.plugin.example.webhook;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("rfi-plugin-example")
public interface WebhookHandler {

    @GET
    @Path("/ping")
    Response ping();

    @POST
    @Path("/hook")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    Response handleWebhook(String body);

    @GET
    @Path("/sync/{alias}")
    @Produces({MediaType.APPLICATION_JSON})
    Response sync(@PathParam("alias") String alias);

    @GET
    @Path("/connections")
    @Produces({MediaType.APPLICATION_JSON})
    List<ConnectionDto> getConnectionList();

    @GET
    @Path("/connections/{alias}")
    @Produces({MediaType.APPLICATION_JSON})
    ConnectionDto getConnection(@PathParam("alias") String alias);

    @POST
    @Path("/connections")
    @Consumes({MediaType.APPLICATION_JSON})
    Response addConnection(ConnectionDto connectionDto);

    @PUT
    @Path("/connections/{alias}")
    @Consumes({MediaType.APPLICATION_JSON})
    Response updateConnection(@PathParam("alias") String alias, ConnectionDto connectionDto);

    @DELETE
    @Path("/connections/{alias}")
    Response deleteConnection(@PathParam("alias") String alias);

}
