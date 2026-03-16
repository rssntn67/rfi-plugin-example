package it.xeniaprogetti.rfi.plugin.example.provisioning;



import java.util.Objects;

public class RequestContext {

    private final Request request;

    public RequestContext(final Request request) {
        this.request = Objects.requireNonNull(request);
    }

    public Request getRequest() {
        return this.request;
    }

    public String getPath() {
        return this.request.getPath();
    }

}
