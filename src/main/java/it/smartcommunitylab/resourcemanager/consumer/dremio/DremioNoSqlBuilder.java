package it.smartcommunitylab.resourcemanager.consumer.dremio;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.smartcommunitylab.resourcemanager.common.NoSuchConsumerException;
import it.smartcommunitylab.resourcemanager.model.Consumer;
import it.smartcommunitylab.resourcemanager.model.ConsumerBuilder;
import it.smartcommunitylab.resourcemanager.model.Registration;

@Component
public class DremioNoSqlBuilder implements ConsumerBuilder {

    @Value("${consumers.dremio.enable}")
    private boolean enabled;

    @Value("${consumers.dremio.properties}")
    private List<String> properties;

    @Override
    public String getType() {
        return DremioNoSqlConsumer.TYPE;
    }

    @Override
    public String getId() {
        return DremioNoSqlConsumer.ID;
    }

    @Override
    public Set<String> listProperties() {
        return new HashSet<String>(properties);
    }

    @Override
    public boolean isAvailable() {
        return enabled;
    }

    @Override
    public Consumer build() throws NoSuchConsumerException {
        // not supported
        throw new NoSuchConsumerException();
    }

    @Override
    public Consumer build(Map<String, Serializable> properties) throws NoSuchConsumerException {
        // properties supported
        DremioNoSqlConsumer consumer = new DremioNoSqlConsumer(properties);
        // explicitly call init() since @postconstruct won't work here
        consumer.init();

        return consumer;
    }

    @Override
    public Consumer build(Registration reg) throws NoSuchConsumerException {
        // properties supported
        DremioNoSqlConsumer consumer = new DremioNoSqlConsumer(reg);
        // explicitly call init() since @postconstruct won't work here
        consumer.init();

        return consumer;
    }
}