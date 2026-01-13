package it.xeniaprogetti.rfi.plugin.example.provisioning;

import org.opennms.integration.api.v1.requisition.RequisitionRequest;

import java.util.Objects;


public class Request implements RequisitionRequest {

    private String path;

    public Request(String path) {
        this.path = Objects.requireNonNull(path);
    }

    public String getPath() {
        return path;
    }
}
