package it.smartcommunitylab.resourcemanager.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import it.smartcommunitylab.resourcemanager.model.Registration;
import it.smartcommunitylab.resourcemanager.serializer.ConsumerDeserializer;
import it.smartcommunitylab.resourcemanager.serializer.ConsumerSerializer;

@JsonSerialize(using = ConsumerSerializer.class)
@JsonDeserialize(using = ConsumerDeserializer.class)
public class ConsumerDTO {

	public long id;
	public String userId;
	public String scopeId;

	public String type;
	public String consumer;

	public String properties;

	public ConsumerDTO() {
		id = 0;
		userId = "";

		type = "";
		consumer = "";

		properties = "{}";
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getConsumer() {
		return consumer;
	}

	public void setConsumer(String consumer) {
		this.consumer = consumer;
	}

	public String getProperties() {
		return properties;
	}

	public void setProperties(String properties) {
		this.properties = properties;
	}

	@Override
	public String toString() {
		return "ConsumerDTO [id=" + id + ", userId=" + userId + ", type=" + type + ", consumer=" + consumer
				+ ", properties=" + properties + "]";
	}

	public static ConsumerDTO fromRegistration(Registration reg) {
		ConsumerDTO dto = new ConsumerDTO();
		dto.id = reg.getId();
		dto.userId = reg.getUserId();

		dto.type = reg.getType();
		dto.consumer = reg.getConsumer();

		dto.properties = reg.getProperties();
		if (dto.properties.isEmpty()) {
			dto.properties = "{}";
		}

		return dto;
	}
}
