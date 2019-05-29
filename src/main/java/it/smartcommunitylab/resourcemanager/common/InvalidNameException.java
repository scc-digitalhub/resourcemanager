package it.smartcommunitylab.resourcemanager.common;

public class InvalidNameException extends Exception {

    private static final long serialVersionUID = -968088803328660701L;

    public InvalidNameException() {
        super("invalid name");
    }

    public InvalidNameException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidNameException(String message) {
        super(message);
    }

    public InvalidNameException(Throwable cause) {
        super(cause);
    }
}