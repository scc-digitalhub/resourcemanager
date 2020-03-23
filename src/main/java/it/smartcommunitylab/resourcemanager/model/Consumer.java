package it.smartcommunitylab.resourcemanager.model;

import it.smartcommunitylab.resourcemanager.common.ConsumerException;

public abstract class Consumer {

	/*
	 * Consumer
	 */
	public abstract String getId();

	public abstract String getType();
	
	public abstract String getUrl();

	public abstract int getStatus();

	public abstract Registration getRegistration();

	/*
	 * Resources
	 */
	public abstract void addResource(String spaceId, String userId, Resource resource) throws ConsumerException;

	public abstract void checkResource(String spaceId, String userId, Resource resource) throws ConsumerException;

	public abstract void updateResource(String spaceId, String userId, Resource resource) throws ConsumerException;

	public abstract void deleteResource(String spaceId, String userId, Resource resource) throws ConsumerException;

}