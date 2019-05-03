package it.smartcommunitylab.resourcemanager.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import it.smartcommunitylab.resourcemanager.dto.ConsumerDTO;

public class ConsumerDeserializer extends StdDeserializer<ConsumerDTO> {

	private static final long serialVersionUID = 7325201711034533646L;

	public ConsumerDeserializer() {
		this(null);
	}

	public ConsumerDeserializer(Class<?> t) {
		super(t);
	}

	@Override
	public ConsumerDTO deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {

		JsonNode node = jp.getCodec().readTree(jp);
		ConsumerDTO dto = new ConsumerDTO();

		if (node.has("id")) {
			dto.id = node.get("id").asLong();
		}
		if (node.has("userId")) {
			dto.userId = node.get("userId").asText();
		}

		if (node.has("type")) {
			dto.type = node.get("type").asText();
		}
		if (node.has("consumer")) {
			dto.consumer = node.get("consumer").asText();
		}

		JsonNode properties = node.get("properties");
		if (properties != null) {
			dto.setProperties(properties.toString());
		}

		return dto;
	}
}
