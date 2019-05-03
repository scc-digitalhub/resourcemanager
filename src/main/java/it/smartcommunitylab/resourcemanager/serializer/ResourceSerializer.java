package it.smartcommunitylab.resourcemanager.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import it.smartcommunitylab.resourcemanager.dto.ResourceDTO;

public class ResourceSerializer extends StdSerializer<ResourceDTO> {

	private static final long serialVersionUID = 8183246348939784272L;

	public ResourceSerializer() {
		this(null);
	}

	public ResourceSerializer(Class<ResourceDTO> t) {
		super(t);
	}

	@Override
	public void serialize(
			ResourceDTO resource, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {

		jgen.writeStartObject();
		jgen.writeNumberField("id", resource.getId());

		jgen.writeStringField("type", resource.getType());
		jgen.writeStringField("provider", resource.getProvider());
		jgen.writeStringField("uri", resource.getUri());

		jgen.writeStringField("userId", resource.getUserId());

		//write properties json
		jgen.writeFieldName("properties");
		jgen.writeRawValue(resource.getProperties());
		
		jgen.writeEndObject();
	}
}
