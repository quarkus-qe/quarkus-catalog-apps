package io.quarkus.qe.exceptions;

public class EnrichmentException extends Exception {

    private static final long serialVersionUID = 7916673718011285512L;

    public EnrichmentException(String cause, Object... arguments) {
        super(String.format(cause, arguments));
    }

}
