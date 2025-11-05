package it.xeniaprogetti.rfi.plugin.example.connection;

import java.util.Objects;

public class ConnectionValidationError {

    public final String message;

    public ConnectionValidationError(final String message) {
        this.message = Objects.requireNonNull(message);
    }
}
