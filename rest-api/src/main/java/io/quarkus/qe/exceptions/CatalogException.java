package io.quarkus.qe.exceptions;

public class CatalogException extends Exception {

    private static final long serialVersionUID = 7379574120935739461L;

    private final int errorCode;

    public CatalogException(String msg, int errorCode) {
        super(msg);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

}
