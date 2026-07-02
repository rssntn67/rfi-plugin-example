package it.xeniaprogetti.rfi.plugin.example.clients;

import org.opennms.integration.api.v1.ticketing.Ticket;

import java.util.List;

public interface OtrsClient {
    List<Ticket> getAll();
    Ticket get(String ticketId);
    String savaORUpdate(Ticket ticket);
}
