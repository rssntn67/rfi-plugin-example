package it.xeniaprogetti.rfi.plugin.example.webhook;

public class ConnectionDto {

    private String alias;
    private String address; // udp:<ip>/port
    private String community;
    private  int version;
    private String domain;

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getAlias() {
        return alias;
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

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }
}
