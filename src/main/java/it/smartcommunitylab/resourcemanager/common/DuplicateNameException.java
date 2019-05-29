package it.smartcommunitylab.resourcemanager.common;

public class DuplicateNameException extends Exception {

    private static final long serialVersionUID = 2738286155915616698L;

    public DuplicateNameException() {
        super("duplicated name");
    }

    public DuplicateNameException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateNameException(String message) {
        super(message);
    }

    public DuplicateNameException(Throwable cause) {
        super(cause);
    }
}
