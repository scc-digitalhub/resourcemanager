package it.smartcommunitylab.resourcemanager;

public class SystemKeys {

	public final static String TYPE_SQL = "sql";
	public final static String TYPE_NOSQL = "nosql";
	public final static String TYPE_OBJECT = "object";
	public final static String TYPE_FILE = "file";

	public final static int STATUS_READY = 0;
	public final static int STATUS_INIT = 1;
	public final static int STATUS_ERROR = 2;
	public final static int STATUS_UNKNOWN = 3;
	public final static int STATUS_DISABLED = -1;

	public final static String ACTION_CREATE = "create";
	public final static String ACTION_UPDATE = "update";
	public final static String ACTION_DELETE = "delete";
	public final static String ACTION_CHECK = "check";

	public final static String CONFIG_PROVIDERS = "system.providers";
	public final static String CONFIG_CONSUMERS = "system.consumers";

	public final static String ORDER_ASC = "asc";
	public final static String ORDER_DESC = "desc";

}
