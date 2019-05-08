package it.smartcommunitylab.resourcemanager.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import it.smartcommunitylab.resourcemanager.common.ResourceProviderException;

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
	public abstract Resource createResource(String scopeId, String userId, Map<String, Serializable> properties)
			throws ResourceProviderException;

	public abstract void updateResource(Resource resource) throws ResourceProviderException;

	public abstract void deleteResource(Resource resource) throws ResourceProviderException;

	public abstract void checkResource(Resource resource) throws ResourceProviderException;

	/*
	 * Properties
	 */
	public abstract Set<String> listProperties();

}
