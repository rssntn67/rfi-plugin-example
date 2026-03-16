package it.xeniaprogetti.rfi.plugin.example.provisioning.desigo;

public class DesigoNode {

    private String code;
    private String name;

    public DesigoNode(){
    }

    public DesigoNode(String code, String name){
        this.code = code;
        this.name = name;
    }

    public String getCode(){
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
