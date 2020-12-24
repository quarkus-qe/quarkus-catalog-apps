package io.quarkus.qe.exceptions;

public class RepositoryAlreadyExistsException extends CatalogException {

    public static final int UNIQUE_SERVICE_ERROR_ID = 001;

    private static final long serialVersionUID = 8119616657480723651L;

    public RepositoryAlreadyExistsException(String msg) {
        super(msg, UNIQUE_SERVICE_ERROR_ID);
    }

}
