package it.smartcommunitylab.resourcemanager.dto;

import java.util.Set;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.smartcommunitylab.resourcemanager.model.ConsumerBuilder;

@ApiModel(value = "Builder")
public class BuilderDTO {

	@ApiModelProperty(notes = "Builder resource type", example = "sql")
	public String type;

	@ApiModelProperty(notes = "Builder consumer name", example = "dremio")
	public String consumer;

	@ApiModelProperty(notes = "Consumer properties map - class specific", example = "{}")
	public String[] properties;

	public BuilderDTO() {

	}

	public BuilderDTO(String type, String consumer) {
		super();
		this.type = type;
		this.consumer = consumer;
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

	public String[] getProperties() {
		return properties;
	}

	public void setProperties(String[] properties) {
		this.properties = properties;
	}

	@Override
	public String toString() {
		return "BuilderDTO [type=" + type + ", consumer=" + consumer + "]";
	}

	public static BuilderDTO fromBuilder(ConsumerBuilder cb) {

		BuilderDTO dto = new BuilderDTO(cb.getType(), cb.getId());
		Set<String> prop = cb.listProperties();

		dto.properties = prop.toArray(new String[0]);

		return dto;

	}
}
