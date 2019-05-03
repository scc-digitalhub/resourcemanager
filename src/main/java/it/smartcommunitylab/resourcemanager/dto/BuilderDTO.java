package it.smartcommunitylab.resourcemanager.dto;

public class BuilderDTO {

	public String type;
	public String consumer;

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

	@Override
	public String toString() {
		return "BuilderDTO [type=" + type + ", consumer=" + consumer + "]";
	}

}
