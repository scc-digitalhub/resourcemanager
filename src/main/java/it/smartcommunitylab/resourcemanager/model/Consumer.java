package it.smartcommunitylab.resourcemanager.model;

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
	public abstract void addResource(String scopeId, String userId, Resource resource);

	public abstract void checkResource(String scopeId, String userId, Resource resource);

	public abstract void updateResource(String scopeId, String userId, Resource resource);

	public abstract void deleteResource(String scopeId, String userId, Resource resource);

}