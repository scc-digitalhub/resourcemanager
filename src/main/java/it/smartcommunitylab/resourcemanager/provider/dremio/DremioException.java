package it.smartcommunitylab.resourcemanager.provider.dremio;

public class DremioException extends Exception {

    private static final long serialVersionUID = 7561145894516953269L;

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
