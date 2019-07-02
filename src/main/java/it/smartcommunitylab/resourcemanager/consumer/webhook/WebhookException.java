package it.smartcommunitylab.resourcemanager.consumer.webhook;

public class WebhookException extends Exception {

    private static final long serialVersionUID = 6685935385306929776L;

    public WebhookException() {
        super();
    }

    public WebhookException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebhookException(String message) {
        super(message);
    }

    public WebhookException(Throwable cause) {
        super(cause);
    }
}
