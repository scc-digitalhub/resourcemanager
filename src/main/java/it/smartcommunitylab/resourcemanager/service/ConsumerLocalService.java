package it.smartcommunitylab.resourcemanager.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import it.smartcommunitylab.resourcemanager.SystemKeys;
import it.smartcommunitylab.resourcemanager.common.NoSuchConsumerException;
import it.smartcommunitylab.resourcemanager.common.NoSuchProviderException;
import it.smartcommunitylab.resourcemanager.common.NoSuchRegistrationException;
import it.smartcommunitylab.resourcemanager.common.NoSuchResourceException;
import it.smartcommunitylab.resourcemanager.common.ResourceProviderException;
import it.smartcommunitylab.resourcemanager.config.ConsumerConfiguration;
import it.smartcommunitylab.resourcemanager.model.Consumer;
import it.smartcommunitylab.resourcemanager.model.ConsumerBuilder;
import it.smartcommunitylab.resourcemanager.model.Registration;
import it.smartcommunitylab.resourcemanager.model.Resource;
import it.smartcommunitylab.resourcemanager.model.ResourceEvent;

@Component
public class ConsumerLocalService {
	private final static Logger _log = LoggerFactory.getLogger(ConsumerLocalService.class);

	private Map<String, List<Consumer>> _consumers;

	@Autowired
	private ConsumerConfiguration staticConfig;

	@Autowired
	private ResourceLocalService resourceLocalService;

	@Autowired
	private RegistrationLocalService registrationService;

	/*
	 * Init
	 */
	@PostConstruct
	private void init() {
		// init consumer in-memory map
		// TODO replace with better repo
		_consumers = new HashMap<>();
		// init types - TODO add configuration
		_consumers.put(SystemKeys.TYPE_SQL, new ArrayList<>());
		_consumers.put(SystemKeys.TYPE_NOSQL, new ArrayList<>());
		_consumers.put(SystemKeys.TYPE_FILE, new ArrayList<>());
		_consumers.put(SystemKeys.TYPE_OBJECT, new ArrayList<>());

		// scan static consumers from config
		// TODO refactor
		for (String type : _consumers.keySet()) {

			for (String id : staticConfig.get(type)) {
				// create via builder
				try {
					Consumer c = buildConsumer(id);
					// add to map
					_consumers.get(type).add(c);
				} catch (NoSuchConsumerException e) {
					_log.error("no builder for " + id);
				}
			}
		}

		// read consumers from DB
		List<Registration> registrations = registrationService.list();
		for (Registration reg : registrations) {
			// build consumer with properties
			try {
				Consumer c = buildConsumer(reg);
				// add to map
				_consumers.get(reg.getType()).add(c);
			} catch (NoSuchConsumerException e) {
				_log.error("no builder for registration " + reg.getId());
			}
		}

		// check all resources
		checkResources();
	}

	public void checkResources() {
		// fetch all resources and require check for both producer and consumers
		List<Resource> resources = resourceLocalService.list();
		for (Resource r : resources) {
			try {
				resourceLocalService.check(r.getId());
			} catch (NoSuchResourceException | NoSuchProviderException e) {
				_log.error("error checking resource " + r.getId());
			} catch (ResourceProviderException e) {
				_log.error("provider error checking resource " + r.getId());
			}
		}
	}

	/*
	 * Builders
	 */

	@Autowired
	private Map<String, ConsumerBuilder> _builders;

	private Consumer buildConsumer(String id) throws NoSuchConsumerException {
		// build without parameters
		return getBuilder(id).build();
	}

	private Consumer buildConsumer(Registration reg) throws NoSuchConsumerException {

		// lookup builder
		String id = reg.getConsumer();
		// build based on registration
		return getBuilder(id).build(reg);
	}

	private Consumer buildConsumer(String id, Map<String, Serializable> properties) throws NoSuchConsumerException {

		// build based on properties
		return getBuilder(id).build(properties);
	}

	public ConsumerBuilder getBuilder(String id) throws NoSuchConsumerException {

		// check if id ends with "Consumer"
		// spring registers beans with "className" as key
		// code expects consumer classes to end with *Consumer.java
		// builders should be named as [consumerId]Builder.java
		if (!id.endsWith("Consumer")) {
			id = id.concat("Consumer");
		}
		String builderClass = id.replace("Consumer", "Builder");

		if (!_builders.containsKey(builderClass)) {
			throw new NoSuchConsumerException();
		}

		return _builders.get(builderClass);
	}

	public boolean hasBuilder(String id) {
		// check if id ends with "Consumer"
		// spring registers beans with "className" as key
		// code expects consumer classes to end with *Consumer.java
		// builders should be named as [consumerId]Builder.java
		if (!id.endsWith("Consumer")) {
			id = id.concat("Consumer");
		}
		String builderClass = id.replace("Consumer", "Builder");

		return _builders.containsKey(builderClass);
	}

	public Map<String, List<String>> listBuilders() {
		Map<String, List<String>> map = new HashMap<>();
		// static init for all types
		map.put(SystemKeys.TYPE_SQL, new ArrayList<>());
		map.put(SystemKeys.TYPE_NOSQL, new ArrayList<>());
		map.put(SystemKeys.TYPE_FILE, new ArrayList<>());
		map.put(SystemKeys.TYPE_OBJECT, new ArrayList<>());

		for (ConsumerBuilder b : _builders.values()) {
			if (b.isAvailable()) {
				map.get(b.getType()).add(b.getId());
			}
		}

		return map;
	}

	public List<String> listBuilders(String type) {
		return listBuilders().get(type);
	}

	/*
	 * Data
	 */

	public Registration add(String scopeId, String userId, String type, String consumer,
			Map<String, Serializable> properties)
			throws NoSuchConsumerException {

		// check support
		if (!hasBuilder(consumer)) {
			throw new NoSuchConsumerException();
		}

		// build registration
		Registration reg = registrationService.add(scopeId, userId, type, consumer, properties);

		// build consumer
		Consumer c = buildConsumer(reg);
		_consumers.get(type).add(c);

		return reg;
	}

	public void delete(long id) throws NoSuchConsumerException {
		try {
			// fetch registration
			Registration reg = registrationService.get(id);
			String type = reg.getType();

			// lookup for consumer
			// TODO replace with lookup map?
			Consumer consumer = null;
			for (Consumer c : _consumers.get(type)) {
				if (c.getRegistration() == reg) {
					consumer = c;
					break;
				}
			}

			if (consumer != null) {
				// delete consumer - no cleanup or shutdown
				_consumers.get(type).remove(consumer);
			}

			// clear registration
			registrationService.delete(id);
		} catch (NoSuchRegistrationException nrex) {
			throw new NoSuchConsumerException();
		}
	}

	/*
	 * Events
	 */
	@EventListener
	public void receiveResourceEvent(ResourceEvent event) {
		_log.info("receive message for " + event.getType() + " with payload " + event.getAction() + ":"
				+ String.valueOf(event.getId()));

		try {
			// fetch resource
			Resource res = resourceLocalService.get(event.getId());
			String type = event.getType();
			String action = event.getAction();
			String scopeId = event.getScopeId();
			String userId = event.getUserId();

			// notify all active consumers
			// note: filtering by user is performed by consumers, IF required/implemented
			for (Consumer c : _consumers.get(type)) {
				if (c.getStatus() == SystemKeys.STATUS_READY) {
					_log.info("notify consumer " + c.getId() + " for " + event.getType() + " with payload "
							+ event.getAction() + ":" + String.valueOf(event.getId()));

					switch (action) {
					case SystemKeys.ACTION_CREATE:
						c.addResource(scopeId, userId, res);
						break;
					case SystemKeys.ACTION_UPDATE:
						c.updateResource(scopeId, userId, res);
						break;
					case SystemKeys.ACTION_DELETE:
						c.deleteResource(scopeId, userId, res);
						break;
					case SystemKeys.ACTION_CHECK:
						c.checkResource(scopeId, userId, res);
						break;
					}

				}
			}

		} catch (NoSuchResourceException e) {
			_log.error("resource missing for event");
		}

	}
}
