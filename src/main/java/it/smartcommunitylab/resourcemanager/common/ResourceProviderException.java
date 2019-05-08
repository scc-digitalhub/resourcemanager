package it.smartcommunitylab.resourcemanager.common;

public class ResourceProviderException extends Exception {

	private static final long serialVersionUID = 5766228223293389007L;

	public ResourceProviderException() {
		super();
	}

	public ResourceProviderException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResourceProviderException(String message) {
		super(message);
	}

	public ResourceProviderException(Throwable cause) {
		super(cause);
	}
}