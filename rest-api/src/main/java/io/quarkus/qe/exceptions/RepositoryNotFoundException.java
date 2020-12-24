package io.quarkus.qe.exceptions;

public class RepositoryNotFoundException extends CatalogException {

    public static final int UNIQUE_SERVICE_ERROR_ID = 002;

    private static final long serialVersionUID = -5357444829957725283L;

    public RepositoryNotFoundException(String msg) {
        super(msg, UNIQUE_SERVICE_ERROR_ID);
    }
}
