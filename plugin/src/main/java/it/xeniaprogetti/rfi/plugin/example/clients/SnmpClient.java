package it.xeniaprogetti.rfi.plugin.example.clients;

import it.xeniaprogetti.rfi.plugin.example.connection.Connection;
import it.xeniaprogetti.rfi.plugin.example.snmp.AdvancedSnmpSet;

import java.io.IOException;

public class SnmpClient {

    public AdvancedSnmpSet getSnmpClient(final Connection connection) throws IOException {

        return new AdvancedSnmpSet(getCredential(connection));
    }

    private SnmpCredentials getCredential(final Connection connection){

        return SnmpCredentials.builder()
                .withAddress(connection.getAddress())
                .withCommunity(connection.getCommunity())
                .withVersion(connection.getVersion())
                .withIpAddr(connection.getAlias()) //alias = ip
                .build();
    }
}
