package it.xeniaprogetti.rfi.plugin.example.provisioning.pbh;

public class PBHNode {

    private String dtp;
    private String nomeImpianto;
    private String tipoImpianto;
    private String matricola;
    private String sedeImpianto;
    private String comune;
    private String provincia;
    private String iccid;
    private String msisdn;

    public PBHNode() {
    }

    public PBHNode(String dtp, String nomeImpianto, String tipoImpianto, String matricola, String sedeImpianto, String comune, String provincia, String iccid, String msisdn) {
        this.dtp = dtp;
        this.nomeImpianto = nomeImpianto;
        this.tipoImpianto = tipoImpianto;
        this.matricola = matricola;
        this.sedeImpianto = sedeImpianto;
        this.comune = comune;
        this.provincia = provincia;
        this.iccid = iccid;
        this.msisdn = msisdn;
    }

    public String getDtp() {
        return dtp;
    }

    public void setDtp(String dtp) {
        this.dtp = dtp;
    }

    public String getNomeImpianto() {
        return nomeImpianto;
    }

    public void setNomeImpianto(String nomeImpianto) {
        this.nomeImpianto = nomeImpianto;
    }

    public String getTipoImpianto() {
        return tipoImpianto;
    }

    public void setTipoImpianto(String tipoImpianto) {
        this.tipoImpianto = tipoImpianto;
    }

    public String getMatricola() {
        return matricola;
    }

    public void setMatricola(String matricola) {
        this.matricola = matricola;
    }

    public String getSedeImpianto() {
        return sedeImpianto;
    }

    public void setSedeImpianto(String sedeImpianto) {
        this.sedeImpianto = sedeImpianto;
    }

    public String getComune() {
        return comune;
    }

    public void setComune(String comune) {
        this.comune = comune;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }
}
