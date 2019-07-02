package it.smartcommunitylab.resourcemanager.consumer.dss;

public class DSSException extends Exception {

    private static final long serialVersionUID = -3161274795185112429L;

    public DSSException() {
        super();
    }

    public DSSException(String message, Throwable cause) {
        super(message, cause);
    }

    public DSSException(String message) {
        super(message);
    }

    public DSSException(Throwable cause) {
        super(cause);
    }
}
