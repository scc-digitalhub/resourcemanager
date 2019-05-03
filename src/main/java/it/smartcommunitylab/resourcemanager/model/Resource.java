package it.smartcommunitylab.resourcemanager.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.Transient;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import it.smartcommunitylab.resourcemanager.serializer.ResourceDeserializer;
import it.smartcommunitylab.resourcemanager.serializer.ResourceSerializer;

import static javax.persistence.TemporalType.TIMESTAMP;

@Entity
@EntityListeners({ AuditingEntityListener.class, ResourceListener.class })
@JsonSerialize(using = ResourceSerializer.class)
@JsonDeserialize(using = ResourceDeserializer.class)
public class Resource {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String type;
	private String provider;
	private String uri;

	private String userId;
	// example scope=tenant/project/user
	private String scopeId;
	private String properties;

	/*
	 * Audit
	 */
	@Column(name = "created_date", nullable = false, updatable = false)
	@CreatedDate
	@Temporal(TIMESTAMP)
	private Date createdDate;

	@Column(name = "modified_date")
	@LastModifiedDate
	@Temporal(TIMESTAMP)
	private Date modifiedDate;

	@Column(name = "created_by")
	@CreatedBy
	protected String createdBy;

	@Column(name = "modified_by")
	@LastModifiedBy
	protected String lastModifiedBy;

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

	public String getScopeId() {
		return scopeId;
	}

	public void setScopeId(String scopeId) {
		this.scopeId = scopeId;
	}

	public String getProperties() {
		return properties;
	}

	public void setProperties(String properties) {
		this.properties = properties;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	@Override
	public String toString() {
		return "Resource [id=" + id + ", type=" + type + ", provider=" + provider + ", uri=" + uri + ", userId="
				+ userId + ", scopeId=" + scopeId + ", createdDate=" + createdDate + ", modifiedDate=" + modifiedDate
				+ ", createdBy=" + createdBy + ", lastModifiedBy=" + lastModifiedBy + "]";
	}

	@Transient
	@JsonIgnore
	private Map<String, Serializable> map;

	@JsonIgnore
	public Map<String, Serializable> getPropertiesMap() {
		if (this.map == null) {
			// read map from properties
			map = Resource.propertiesFromValue(properties);
		}

		return map;
	}

	public void setPropertiesMap(Map<String, Serializable> map) {
		this.map = map;
		sync();
	}

	@PrePersist
	@PreUpdate
	private void sync() {
		if (map != null) {
			// custom build json from map
			JSONObject json = Resource.jsonFromMap(map);
			// serialize to string
			properties = json.toString();
		} else {
			properties = "{}";
		}
	}

	public static Map<String, Serializable> propertiesFromValue(String value) {
		// read map from string as json
		Map<String, Serializable> map = new HashMap<>();
		JSONObject json = new JSONObject(value);
		// build map from json
		for (String key : json.keySet()) {
			JSONArray arr = json.optJSONArray(key);
			if (arr != null) {
				// value is array of String
				String[] ss = new String[arr.length()];
				for (int i = 0; i < arr.length(); i++) {
					String s = arr.optString(i);
					ss[i] = s;
				}

				map.put(key, ss);
			} else {
				// get as String
				String s = json.optString(key);
				map.put(key, s);
			}
		}

		return map;
	}

	public static JSONObject jsonFromMap(Map<String, Serializable> map) {
		// custom build json from map
		JSONObject json = new JSONObject();
		for (String key : map.keySet()) {
			Serializable value = map.get(key);
			// support only String or String[]
			if (value instanceof String) {
				json.put(key, value);
			} else if (value instanceof String[]) {
				JSONArray arr = new JSONArray();
				for (String s : (String[]) value) {
					arr.put(s);
				}

				json.put(key, arr);
			}
		}

		return json;
	}
}
