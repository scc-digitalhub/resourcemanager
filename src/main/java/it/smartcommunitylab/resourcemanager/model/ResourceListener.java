package it.smartcommunitylab.resourcemanager.model;

import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PreRemove;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.smartcommunitylab.resourcemanager.SystemKeys;
import it.smartcommunitylab.resourcemanager.event.ResourceEventHandler;

@Component
public class ResourceListener {
	/*
	 * Callbacks
	 */
	@PostPersist
	private void postPersist(final Resource entity) {
		service.notifyAction(entity.getScopeId(), entity.getUserId(), entity.getType(), entity.getId(),
				SystemKeys.ACTION_CREATE);
	}

	@PostUpdate
	private void postUpdate(final Resource entity) {
		service.notifyAction(entity.getScopeId(), entity.getUserId(), entity.getType(), entity.getId(),
				SystemKeys.ACTION_UPDATE);
	}

	@PreRemove
	private void preRemove(final Resource entity) {
	    //disabled due to async dispatch
//		service.notifyAction(entity.getScopeId(), entity.getUserId(), entity.getType(), entity.getId(),
//				SystemKeys.ACTION_DELETE);
	}

	/*
	 * Service
	 */
	private ResourceEventHandler service;

	@Autowired
	public ResourceListener(ResourceEventHandler rs) {
		service = rs;
	}
}
