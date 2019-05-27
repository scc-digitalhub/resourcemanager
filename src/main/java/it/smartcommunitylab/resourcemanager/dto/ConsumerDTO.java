package it.smartcommunitylab.resourcemanager.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.smartcommunitylab.resourcemanager.SystemKeys;
import it.smartcommunitylab.resourcemanager.model.Consumer;
import it.smartcommunitylab.resourcemanager.model.Registration;
import it.smartcommunitylab.resourcemanager.serializer.ConsumerDeserializer;
import it.smartcommunitylab.resourcemanager.serializer.ConsumerSerializer;

@JsonSerialize(using = ConsumerSerializer.class)
@JsonDeserialize(using = ConsumerDeserializer.class)
@ApiModel(value = "Consumer")
public class ConsumerDTO {

    @ApiModelProperty(notes = "Consumer ID - autogenerated", example = "123")
    public long id;

    @ApiModelProperty(notes = "Consumer owner Id", example = "admin@local")
    public String userId;

    @ApiModelProperty(notes = "Consumer scope Id", example = "default")
    public String scopeId;

    @ApiModelProperty(notes = "Consumer resource type", example = "sql")
    public String type;

    @ApiModelProperty(notes = "Consumer name", example = "dremio")
    public String consumer;

    @ApiModelProperty(notes = "Consumer status", example = "0")
    public String status;

    @ApiModelProperty(notes = "Consumer access url", example = "http://localhost")
    public String url;

    @ApiModelProperty(notes = "Consumer properties map - class specific", example = "{}")
    public String properties;

    @ApiModelProperty(notes = "Consumer tags", example = "test")
    public String[] tags;

    public ConsumerDTO() {
        id = 0;
        userId = "";

        type = "";
        consumer = "";

        properties = "{}";
        tags = new String[0];

        url = "";
        status = "";
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getScopeId() {
        return scopeId;
    }

    public void setScopeId(String scopeId) {
        this.scopeId = scopeId;
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

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
        dto.scopeId = reg.getScopeId();

        dto.type = reg.getType();
        dto.consumer = reg.getConsumer();

        // clear private fields
        dto.properties = "{}";

        dto.tags = reg.getTags().toArray(new String[0]);

        return dto;
    }

    public static ConsumerDTO fromRegistration(Registration reg, boolean includePrivate) {
        ConsumerDTO dto = fromRegistration(reg);
        if (includePrivate) {

            dto.properties = reg.getProperties();
            if (dto.properties.isEmpty()) {
                dto.properties = "{}";
            }

        }

        return dto;
    }

    public static ConsumerDTO fromConsumer(Consumer consumer, boolean includePrivate) {
        ConsumerDTO dto = fromRegistration(consumer.getRegistration(), includePrivate);
        // ask for url
        dto.url = consumer.getUrl();

        // translate status
        String status = "";
        switch (consumer.getStatus()) {
        case SystemKeys.STATUS_READY:
            status = "ready";
            break;
        case SystemKeys.STATUS_INIT:
            status = "init";
            break;
        case SystemKeys.STATUS_ERROR:
            status = "error";
            break;
        case SystemKeys.STATUS_DISABLED:
            status = "disabled";
            break;
        default:
            status = "unknown";
        }

        dto.status = status;

        return dto;
    }

}
