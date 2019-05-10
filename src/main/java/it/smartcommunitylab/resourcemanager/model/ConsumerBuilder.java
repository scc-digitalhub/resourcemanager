package it.smartcommunitylab.resourcemanager.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import it.smartcommunitylab.resourcemanager.common.ConsumerException;
import it.smartcommunitylab.resourcemanager.common.NoSuchConsumerException;

public interface ConsumerBuilder {

	public String getId();

	public String getType();

	public boolean isAvailable();

	public Consumer build() throws NoSuchConsumerException, ConsumerException;

	public Consumer build(Map<String, Serializable> properties) throws NoSuchConsumerException, ConsumerException;

	public Consumer build(Registration reg) throws NoSuchConsumerException, ConsumerException;

	/*
	 * Properties
	 */
	public abstract Set<String> listProperties();
}
