package it.smartcommunitylab.resourcemanager.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import it.smartcommunitylab.resourcemanager.dto.ResourceDTO;

public class ResourceDeserializer extends StdDeserializer<ResourceDTO> {

	private static final long serialVersionUID = 5583408623910364345L;

	public ResourceDeserializer() {
		this(null);
	}

	public ResourceDeserializer(Class<?> t) {
		super(t);
	}

	@Override
	public ResourceDTO deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {

		JsonNode node = jp.getCodec().readTree(jp);
		ResourceDTO res = new ResourceDTO();

		if (node.has("id")) {
			res.id = node.get("id").asLong();
		}
		if (node.has("scopeId")) {
			res.scopeId = node.get("scopeId").asText();
		}
		if (node.has("userId")) {
			res.userId = node.get("userId").asText();
		}

		if (node.has("type")) {
			res.type = node.get("type").asText();
		}
		if (node.has("provider")) {
			res.provider = node.get("provider").asText();
		}
		if (node.has("uri")) {
			res.uri = node.get("uri").asText();
		}

		JsonNode properties = node.get("properties");
		if (properties != null) {
			res.setProperties(properties.toString());
		}

		return res;
	}

}
