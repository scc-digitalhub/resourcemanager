package it.smartcommunitylab.resourcemanager.consumer.log;

import java.io.Serializable;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.smartcommunitylab.resourcemanager.common.NoSuchConsumerException;
import it.smartcommunitylab.resourcemanager.model.Consumer;
import it.smartcommunitylab.resourcemanager.model.ConsumerBuilder;
import it.smartcommunitylab.resourcemanager.model.Registration;

@Component
public class LogNoSqlBuilder implements ConsumerBuilder {

	@Value("${consumers.log.enable}")
	private boolean enabled;

	private static LogNoSqlConsumer _instance;

	@Override
	public String getType() {
		return LogNoSqlConsumer.TYPE;
	}

	@Override
	public String getId() {
		return LogNoSqlConsumer.ID;
	}

	@Override
	public boolean isAvailable() {
		return enabled;
	}

	@Override
	public Consumer build() throws NoSuchConsumerException {
		if (!enabled) {
			throw new NoSuchConsumerException();
		}

		// use singleton
		if (_instance == null) {
			_instance = new LogNoSqlConsumer();
			// explicitly call init() since @postconstruct won't work here
			_instance.init();

		}

		return _instance;
	}

	@Override
	public Consumer build(Map<String, Serializable> properties) throws NoSuchConsumerException {
		return build();
	}

	@Override
	public Consumer build(Registration reg) throws NoSuchConsumerException {
		return build();
	}

}
