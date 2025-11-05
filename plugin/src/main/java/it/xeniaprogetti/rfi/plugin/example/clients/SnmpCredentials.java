package it.xeniaprogetti.rfi.plugin.example.clients;

import org.snmp4j.mp.SnmpConstants;

public class SnmpCredentials {

    public final String address; // udp:<ip>/port
    public final String community;
    public final int version;

    private SnmpCredentials(final Builder builder) {
        this.address = builder.address;
        this.community = builder.community;
        this.version = builder.version;
    }
    public static Builder builder(){
        return new Builder();
    }

    public static Builder builder(final SnmpCredentials snmpCredentials){
        if(snmpCredentials == null){
            return new Builder();
        }

        return new Builder()
                        .withAddress(snmpCredentials.getAddress())
                        .withCommunity(snmpCredentials.getCommunity())
                        .withVersion(snmpCredentials.getVersion());

    }

    public String getAddress() {
        return address;
    }

    public String getCommunity() {
        return community;
    }

    public int getVersion() {
        return version;
    }

    public static class Builder {
        private String address; // udp:<ip>/port
        private String community;
        private int version;

        private Builder(){}

        public Builder withAddress(String address){
          this.address = address;
          return this;
        }

        public Builder withCommunity(String community){
            this.community = community;
            return this;
        }

        public Builder withVersion(int version){
            this.version = version;
            return this;
        }

        public SnmpCredentials build(){
            return new SnmpCredentials(this);
        }
    }
}
