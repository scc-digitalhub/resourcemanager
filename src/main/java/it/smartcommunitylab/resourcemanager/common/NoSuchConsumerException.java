package it.smartcommunitylab.resourcemanager.common;

public class NoSuchConsumerException extends Exception {

	private static final long serialVersionUID = -1457735751448010942L;

	public NoSuchConsumerException() {
		super();
	}

	public NoSuchConsumerException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoSuchConsumerException(String message) {
		super(message);
	}

	public NoSuchConsumerException(Throwable cause) {
		super(cause);
	}
}