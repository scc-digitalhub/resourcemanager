package it.smartcommunitylab.resourcemanager.consumer.local;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.smartcommunitylab.resourcemanager.SystemKeys;
import it.smartcommunitylab.resourcemanager.common.ConsumerException;
import it.smartcommunitylab.resourcemanager.model.Consumer;
import it.smartcommunitylab.resourcemanager.model.Registration;
import it.smartcommunitylab.resourcemanager.model.Resource;

public class NullSqlConsumer extends Consumer {

	private final static Logger _log = LoggerFactory.getLogger(NullSqlConsumer.class);

	public static final String TYPE = SystemKeys.TYPE_SQL;
	public static final String ID = "nullSql";

	private int STATUS;

	private Registration registration;

	public NullSqlConsumer() {
	}

	public NullSqlConsumer(Map<String, Serializable> properties) {
		_properties = properties;
	}

	// test properties
	private Map<String, Serializable> _properties;

	public Map<String, Serializable> getProperties() {
		return _properties;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public Registration getRegistration() {
		return registration;
	}

	/*
	 * Init method - POST constructor since spring injects properties *after
	 * creation*
	 */
	@PostConstruct
	public void init() {
		_log.debug("init called");

		if (_properties == null) {
			_properties = new HashMap<>();
		}

		STATUS = SystemKeys.STATUS_READY;
	}

	@Override
	public int getStatus() {
		return STATUS;
	}

	@Override
	public void addResource(String scopeId, String userId, Resource resource) throws ConsumerException {
		_log.debug("add resource " + resource.toString());

	}

	@Override
	public void updateResource(String scopeId, String userId, Resource resource) throws ConsumerException {
		_log.debug("update resource " + resource.toString());

	}

	@Override
	public void deleteResource(String scopeId, String userId, Resource resource) throws ConsumerException {
		_log.debug("delete resource " + resource.toString());

	}

	@Override
	public void checkResource(String scopeId, String userId, Resource resource) throws ConsumerException {
		_log.debug("check resource " + resource.toString());

	}
}
