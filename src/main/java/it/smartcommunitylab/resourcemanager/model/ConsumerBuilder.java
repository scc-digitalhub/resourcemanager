package it.smartcommunitylab.resourcemanager.model;

import java.io.Serializable;
import java.util.Map;

import it.smartcommunitylab.resourcemanager.common.NoSuchConsumerException;

public interface ConsumerBuilder {

	public String getId();

	public String getType();

	public boolean isAvailable();

	public Consumer build() throws NoSuchConsumerException;

	public Consumer build(Map<String, Serializable> properties) throws NoSuchConsumerException;

	public Consumer build(Registration reg) throws NoSuchConsumerException;

}
