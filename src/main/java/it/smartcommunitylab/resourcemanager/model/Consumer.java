package it.smartcommunitylab.resourcemanager.model;

import it.smartcommunitylab.resourcemanager.common.ConsumerException;

public abstract class Consumer {

	/*
	 * Consumer
	 */
	public abstract String getId();

	public abstract String getType();

	public abstract int getStatus();

	public abstract Registration getRegistration();

	/*
	 * Resources
	 */
	public abstract void addResource(String scopeId, String userId, Resource resource) throws ConsumerException;

	public abstract void checkResource(String scopeId, String userId, Resource resource) throws ConsumerException;

	public abstract void updateResource(String scopeId, String userId, Resource resource) throws ConsumerException;

	public abstract void deleteResource(String scopeId, String userId, Resource resource) throws ConsumerException;

}