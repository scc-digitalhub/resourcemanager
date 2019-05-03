package it.smartcommunitylab.resourcemanager.common;

public class NoSuchResourceException extends Exception {

	private static final long serialVersionUID = -6996196973678437726L;

	public NoSuchResourceException() {
		super();
	}

	public NoSuchResourceException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoSuchResourceException(String message) {
		super(message);
	}

	public NoSuchResourceException(Throwable cause) {
		super(cause);
	}
}