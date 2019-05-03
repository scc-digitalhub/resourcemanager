package it.smartcommunitylab.resourcemanager.common;

public class NoSuchProviderException extends Exception {

	private static final long serialVersionUID = 5159102079326531210L;

	public NoSuchProviderException() {
		super();
	}

	public NoSuchProviderException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoSuchProviderException(String message) {
		super(message);
	}

	public NoSuchProviderException(Throwable cause) {
		super(cause);
	}
}