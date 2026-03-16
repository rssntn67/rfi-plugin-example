package it.xeniaprogetti.rfi.plugin.example.connection;

public interface Connection {

    String getAlias();

    String getAddress();
    void setAddress(String address);

    String getCommunity();
    void setCommunity(String community);

    int getVersion();
    void setVersion(int version);

    String getDomain();
    //void setDomain(String domain);


    void save();
    void delete();
}
