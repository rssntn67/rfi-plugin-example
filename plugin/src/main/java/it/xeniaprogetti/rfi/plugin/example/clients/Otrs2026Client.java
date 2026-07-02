package it.xeniaprogetti.rfi.plugin.example.clients;

import org.opennms.integration.api.v1.ticketing.Ticket;

import java.util.List;

public class Otrs2026Client implements OtrsClient {
    @Override
    public List<Ticket> getAll() {
        return List.of();
    }

    @Override
    public Ticket get(String ticketId) {
        return null;
    }

    @Override
    public String savaORUpdate(Ticket ticket) {
        return "";
    }
}
