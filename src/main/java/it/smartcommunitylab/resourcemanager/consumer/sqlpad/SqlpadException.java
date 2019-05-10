package it.smartcommunitylab.resourcemanager.consumer.sqlpad;

public class SqlpadException extends Exception {

	private static final long serialVersionUID = -7975095412186198203L;

	public SqlpadException() {
		super();
	}

	public SqlpadException(String message, Throwable cause) {
		super(message, cause);
	}

	public SqlpadException(String message) {
		super(message);
	}

	public SqlpadException(Throwable cause) {
		super(cause);
	}
}
