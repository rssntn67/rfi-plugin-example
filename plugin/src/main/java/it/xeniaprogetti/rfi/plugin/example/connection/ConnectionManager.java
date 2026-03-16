package it.xeniaprogetti.rfi.plugin.example.connection;

import it.xeniaprogetti.rfi.plugin.example.clients.SnmpCredentials;
import org.opennms.integration.api.v1.runtime.Container;
import org.opennms.integration.api.v1.runtime.RuntimeInfo;
import org.opennms.integration.api.v1.scv.Credentials;
import org.opennms.integration.api.v1.scv.SecureCredentialsVault;
import org.opennms.integration.api.v1.scv.immutables.ImmutableCredentials;

import java.util.*;
import java.util.stream.Collectors;

public class ConnectionManager {

    private static final String VERSION_KEY = "version";
    private static final String DOMAIN_KEY = "domain";

    private static final String PREFIX = "rfi_example_connection_";
    private final RuntimeInfo runtimeInfo;
    private final SecureCredentialsVault vault;


    public ConnectionManager(RuntimeInfo runtimeInfo, SecureCredentialsVault vault) {
        this.runtimeInfo = runtimeInfo;
        this.vault = vault;
    }

    /**
     * Returns a connection config for the given alias.
     *
     * @return The connection config or {@code Optional#empty()} of no such alias exists
     */
    public Optional<Connection> getConnection(final String alias) {
        this.ensureCore();

        final var credentials = this.vault.getCredentials(PREFIX + alias);
        if (credentials == null) {
            return Optional.empty();
        }

        if (isNullOrEmpty(credentials.getAttribute(DOMAIN_KEY))) {
            throw new IllegalStateException("Domain is missing");
        }
        String domain = credentials.getAttribute(DOMAIN_KEY);
        ConnectionImpl conn = new ConnectionImpl(fromStore(credentials, alias), alias, domain);
        return Optional.of(conn);
    }

    /**
     * Creates a basic authentication connection under the given alias.
     *
     * @param address        the address of the server. pattern for udp connection: udp:<ip>/port
     * @param community          the community to authenticate the connection
     * @param version          the version to authenticate the connection
     */
    public Connection newConnection(final String address, final String community, final int version, final String alias, final String domain) {
        this.ensureCore();

        return new ConnectionImpl(SnmpCredentials.builder()
                .withAddress(address)
                .withCommunity(community)
                .withVersion(version)
                .withIpAddr(alias)
                .build(), alias, domain);
    }

    /**
     * Deletes a connection under the given alias.
     *
     * @return <b>true</b> if an existing connection with given alias was found and deleted and <b>false</b> if no
     * connection with given alias was not found
     */
    public boolean deleteConnection(final String alias) {
        this.ensureCore();

        final var connection = this.getConnection(alias);
        if (connection.isEmpty()) {
            return false;
        }
        connection.get().delete();
        return true;
    }


    public Set<String> getAliases(){
        this.ensureCore();

        return this.vault.getAliases().stream()
                .filter(alias -> alias.startsWith(PREFIX))
                .map(alias -> alias.substring(PREFIX.length()))
                .collect(Collectors.toSet());
    }

    private static SnmpCredentials fromStore(final Credentials credentials, final String alias) {

        if (isNullOrEmpty(credentials.getUsername())) {
            throw new IllegalStateException("ADDRESS is missing");
        }

        if (isNullOrEmpty(credentials.getPassword())) {
            throw new IllegalStateException("COMMUNITY is missing");
        }

        String versionStr = credentials.getAttribute(VERSION_KEY);
        if (isNullOrEmpty(versionStr) || !versionStr.matches("[0-9]+")) {
            throw new IllegalStateException("Version is missing");
        }

        return SnmpCredentials.builder()
                .withAddress(credentials.getUsername())
                .withCommunity(credentials.getPassword())
                .withVersion(Integer.parseInt(versionStr))
                .withIpAddr(alias) //alias = ip
                .build();
    }

    private class ConnectionImpl implements Connection {

        private SnmpCredentials credentials;
        private final String alias;
        private final String domain;

        private ConnectionImpl(final SnmpCredentials credentials, final String alias, final String domain) {
            this.credentials = Objects.requireNonNull(credentials);
            this.alias = alias;
            this.domain = domain;
        }

        @Override
        public String getAlias() {
            return this.alias;
        }

        @Override
        public String getAddress() {
            return this.credentials.getAddress();
        }

        @Override
        public void setAddress(String address) {
            this.credentials = SnmpCredentials.builder(this.credentials)
                    .withAddress(address)
                    .build();
        }

        @Override
        public String getCommunity() {
            return this.credentials.getCommunity();
        }

        @Override
        public void setCommunity(String community) {
            this.credentials = SnmpCredentials.builder(this.credentials)
                    .withCommunity(community)
                    .build();
        }

        @Override
        public int getVersion() {
            return this.credentials.getVersion();
        }

        @Override
        public void setVersion(int version) {
            this.credentials = SnmpCredentials.builder(this.credentials)
                    .withVersion(version)
                    .build();
        }

        @Override
        public String getDomain(){ return this.domain;}


        @Override
        public void save() {
            ConnectionManager.this.vault.setCredentials(PREFIX + alias, this.asCredentials());
        }

        @Override
        public void delete() {
            ConnectionManager.this.vault.deleteCredentials(PREFIX + alias);
        }

        private Credentials asCredentials() {
            Map<String,String> credentialMap = new HashMap<>();
            credentialMap.put(VERSION_KEY, String.valueOf(this.credentials.version));
            credentialMap.put(DOMAIN_KEY, this.domain);

            return new ImmutableCredentials(this.credentials.address, this.credentials.community, credentialMap);
        }

        @Override
        public String toString() {
            return "ConnectionImpl{" +
                    "credentials=" + credentials +
                    '}';
        }
    }

    public void ensureCore() {
        if (this.runtimeInfo.getContainer() != Container.OPENNMS) {
            throw new IllegalStateException("Operation only allowed on OpenNMS instance");
        }
    }

    private static boolean isNullOrEmpty(String check) {
        return check == null || check.isEmpty();
    }

}
