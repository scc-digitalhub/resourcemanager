package it.smartcommunitylab.resourcemanager.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import it.smartcommunitylab.resourcemanager.model.ResourceEvent;

@Component
public class ResourceEventHandler {
	private final static Logger _log = LoggerFactory.getLogger(ResourceEventHandler.class);

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	public void notifyAction(String scopeId, String userId, String type, long id, String action) {

		_log.debug("create message for " + type + " with payload " + action + ":" + String.valueOf(id));

		// create message
		ResourceEvent event = new ResourceEvent(this, scopeId, userId, type, id, action);
		applicationEventPublisher.publishEvent(event);

	}
}
