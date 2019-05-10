package it.smartcommunitylab.resourcemanager.common;

public class ConsumerException extends Exception {

	private static final long serialVersionUID = -2244105409706649323L;

	public ConsumerException() {
		super();
	}

	public ConsumerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConsumerException(String message) {
		super(message);
	}

	public ConsumerException(Throwable cause) {
		super(cause);
	}
}