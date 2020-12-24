package io.quarkus.qe.exceptions;

public class UnexpectedCatalogException extends CatalogException {

    public static final int UNIQUE_SERVICE_ERROR_ID = 000;

    private static final long serialVersionUID = 4442033229110468176L;

    public UnexpectedCatalogException(String msg) {
        super(msg, UNIQUE_SERVICE_ERROR_ID);
    }
}
