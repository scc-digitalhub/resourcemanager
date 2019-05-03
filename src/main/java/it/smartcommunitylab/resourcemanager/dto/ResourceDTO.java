package it.smartcommunitylab.resourcemanager.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import it.smartcommunitylab.resourcemanager.model.Resource;
import it.smartcommunitylab.resourcemanager.serializer.ResourceDeserializer;
import it.smartcommunitylab.resourcemanager.serializer.ResourceSerializer;

@JsonSerialize(using = ResourceSerializer.class)
@JsonDeserialize(using = ResourceDeserializer.class)
public class ResourceDTO {

	public long id;

	public String type;
	public String provider;
	public String uri;

	public String userId;
	public String scopeId;
	public String properties;

	public ResourceDTO() {
		id = 0;
		userId = "";
		scopeId = "";

		type = "";
		provider = "";
		uri = "";

		properties = "{}";
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getProperties() {
		return properties;
	}

	public void setProperties(String properties) {
		this.properties = properties;
	}

	@Override
	public String toString() {
		return "ResourceDTO [id=" + id + ", type=" + type + ", provider=" + provider + ", uri=" + uri + ", userId="
				+ userId + ", scopeId=" + scopeId + ", properties=" + properties + "]";
	}

	public static ResourceDTO fromResource(Resource res) {
		ResourceDTO dto = new ResourceDTO();
		dto.id = res.getId();
		dto.userId = res.getUserId();
		dto.scopeId = res.getScopeId();

		dto.type = res.getType();
		dto.provider = res.getProvider();
		dto.uri = res.getUri();

		dto.properties = res.getProperties();
		if (dto.properties.isEmpty()) {
			dto.properties = "{}";
		}

		return dto;
	}

}
