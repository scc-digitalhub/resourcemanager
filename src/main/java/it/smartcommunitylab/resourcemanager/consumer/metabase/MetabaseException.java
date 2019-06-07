package it.smartcommunitylab.resourcemanager.consumer.metabase;

public class MetabaseException extends Exception {

    private static final long serialVersionUID = -3732588979106861748L;

    public MetabaseException() {
        super();
    }

    public MetabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public MetabaseException(String message) {
        super(message);
    }

    public MetabaseException(Throwable cause) {
        super(cause);
    }

}
