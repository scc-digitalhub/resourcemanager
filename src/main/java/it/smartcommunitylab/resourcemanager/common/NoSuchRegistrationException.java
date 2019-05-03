package it.smartcommunitylab.resourcemanager.common;

public class NoSuchRegistrationException extends Exception {

	private static final long serialVersionUID = -1457735751448010942L;

	public NoSuchRegistrationException() {
		super();
	}

	public NoSuchRegistrationException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoSuchRegistrationException(String message) {
		super(message);
	}

	public NoSuchRegistrationException(Throwable cause) {
		super(cause);
	}
}