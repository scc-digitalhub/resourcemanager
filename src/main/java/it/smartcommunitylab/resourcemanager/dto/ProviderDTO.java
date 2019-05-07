package it.smartcommunitylab.resourcemanager.dto;

import java.util.Arrays;
import java.util.Set;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.smartcommunitylab.resourcemanager.model.ResourceProvider;

@ApiModel(value = "Provider")
public class ProviderDTO {

	@ApiModelProperty(notes = "Provider resource type", example = "sql")
	public String type;

	@ApiModelProperty(notes = "Provider name", example = "postgres")
	public String provider;

	@ApiModelProperty(notes = "Provider properties map - class specific", example = "{}")
	public String[] properties;

	public ProviderDTO() {
	}

	public ProviderDTO(String type, String provider) {
		super();
		this.type = type;
		this.provider = provider;
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

	public String[] getProperties() {
		return properties;
	}

	public void setProperties(String[] properties) {
		this.properties = properties;
	}

	@Override
	public String toString() {
		return "ProviderDTO [type=" + type + ", provider=" + provider + ", properties=" + Arrays.toString(properties)
				+ "]";
	}

	public static ProviderDTO fromProvider(ResourceProvider p) {

		ProviderDTO dto = new ProviderDTO(p.getType(), p.getId());
		Set<String> prop = p.listProperties();

		dto.properties = prop.toArray(new String[0]);

		return dto;

	}

}
