package it.smartcommunitylab.resourcemanager.provider.local;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.smartcommunitylab.resourcemanager.SystemKeys;
import it.smartcommunitylab.resourcemanager.model.ResourceProvider;
import it.smartcommunitylab.resourcemanager.model.Resource;
import it.smartcommunitylab.resourcemanager.util.SqlUtil;

@Component
public class NullProvider extends ResourceProvider {

	private final static Logger _log = LoggerFactory.getLogger(NullProvider.class);

	public static final String TYPE = SystemKeys.TYPE_SQL;
	public static final String ID = "null";

	private int STATUS;

	@Value("${providers.null.enable}")
	private boolean enabled;

	@Value("${providers.null.properties}")
	private List<String> properties;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public Set<String> listProperties() {
		return new HashSet<String>(properties);
	}

	/*
	 * Init method - POST constructor since spring injects properties *after
	 * creation*
	 */
	@PostConstruct
	public void init() {
		_log.info("enabled " + String.valueOf(enabled));

		if (enabled) {
			STATUS = SystemKeys.STATUS_READY;
		} else {
			STATUS = SystemKeys.STATUS_DISABLED;
		}

		_log.info("init status " + String.valueOf(STATUS));
	}

	@Override
	public int getStatus() {
		return STATUS;
	}

	@Override
	public Resource createResource(String scopeId, String userId, Map<String, Serializable> properties) {
		Resource res = new Resource();
		res.setType(TYPE);
		res.setProvider(ID);
		res.setPropertiesMap(properties);

		// generate uri
		String uri = SqlUtil.encodeURI("null", "host:981", "dbase", "USER", "PASS");
		res.setUri(uri);

		return res;
	}

	@Override
	public void updateResource(Resource resource) {
		// nothing to do

	}

	@Override
	public void deleteResource(Resource resource) {
		// nothing to do

	}

	@Override
	public void checkResource(Resource resource) {
		// nothing to do

	}

}
