package it.smartcommunitylab.resourcemanager.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public abstract class ResourceProvider {

	/*
	 * Provider
	 */
	public abstract String getId();

	public abstract String getType();

	public abstract int getStatus();

	/*
	 * Resources
	 */
	public abstract Resource createResource(String scopeId, String userId, Map<String, Serializable> properties);

	public abstract void updateResource(Resource resource);

	public abstract void deleteResource(Resource resource);

	public abstract void checkResource(Resource resource);

	/*
	 * Properties
	 */
	public abstract Set<String> listProperties();

}
