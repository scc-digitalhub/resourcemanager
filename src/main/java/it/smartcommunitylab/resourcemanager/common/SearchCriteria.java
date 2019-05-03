package it.smartcommunitylab.resourcemanager.common;

public class SearchCriteria {
	public String userId;

	public String type;
	public String consumer;
	public String provider;

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

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	@Override
	public String toString() {
		return "SearchCriteria [userId=" + userId + ", type=" + type + ", consumer=" + consumer + ", provider="
				+ provider + "]";
	}

}
