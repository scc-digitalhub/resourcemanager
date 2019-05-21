package it.smartcommunitylab.resourcemanager.consumer.dremio;

public class DremioException extends Exception {

    private static final long serialVersionUID = -8914131731393882634L;

    public DremioException() {
        super();
    }

    public DremioException(String message, Throwable cause) {
        super(message, cause);
    }

    public DremioException(String message) {
        super(message);
    }

    public DremioException(Throwable cause) {
        super(cause);
    }
}
