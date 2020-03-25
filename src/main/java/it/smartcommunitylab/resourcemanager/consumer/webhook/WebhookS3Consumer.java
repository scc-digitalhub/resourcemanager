package it.smartcommunitylab.resourcemanager.consumer.webhook;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import it.smartcommunitylab.resourcemanager.SystemKeys;
import it.smartcommunitylab.resourcemanager.common.ConsumerException;
import it.smartcommunitylab.resourcemanager.model.Consumer;
import it.smartcommunitylab.resourcemanager.model.Registration;
import it.smartcommunitylab.resourcemanager.model.Resource;
import it.smartcommunitylab.resourcemanager.provider.minio.MinioProvider;
import it.smartcommunitylab.resourcemanager.provider.minio.MinioUtils;

public class WebhookS3Consumer extends Consumer {

    private final static Logger _log = LoggerFactory.getLogger(WebhookS3Consumer.class);

    public static final String TYPE = SystemKeys.TYPE_OBJECT;
    public static final String ID = "webhookobject";

    @Value("${consumers.webhook.credentials}")
    private boolean sendCredentials;

    // endpoint connection
    private String endpoint;

    // basic authentication supported
    private String username;
    private String password;

    // token authentication
    private String token;

    // payload signature
    private String secret;

    private Registration registration;
    private int STATUS;

    // filters
    private String spaceId;
    private List<String> tags;

    private WebhookClient _client;

    public WebhookS3Consumer() {
        endpoint = "";
        username = "";
        password = "";
        token = "";
        secret = "";

        spaceId = "";
        tags = new ArrayList<>();
    }

    public WebhookS3Consumer(Map<String, Serializable> properties) {
        this();
        _properties = properties;
    }

    public WebhookS3Consumer(Registration reg) {
        this();
        registration = reg;
        _properties = reg.getPropertiesMap();
        spaceId = reg.getSpaceId();
        tags = reg.getTags();
    }

    // web hook properties
    private Map<String, Serializable> _properties;

    public Map<String, Serializable> getProperties() {
        return _properties;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getUrl() {
        // no access url
        return "";
    }

    @Override
    public Registration getRegistration() {
        return registration;
    }

    /*
     * Init method - POST constructor since spring injects properties *after
     * creation*
     */
    @PostConstruct
    public void init() {
        _log.debug("init called");

        STATUS = SystemKeys.STATUS_UNKNOWN;

        if (_properties != null) {
            if (_properties.containsKey("endpoint")) {
                endpoint = _properties.get("endpoint").toString();
            }
            if (_properties.containsKey("username")) {
                username = _properties.get("username").toString();
            }
            if (_properties.containsKey("password")) {
                password = _properties.get("password").toString();
            }
            if (_properties.containsKey("token")) {
                token = _properties.get("token").toString();
            }
            if (_properties.containsKey("secret")) {
                secret = _properties.get("secret").toString();
            }
        }

        if (!endpoint.isEmpty()) {
            _client = new WebhookClient(endpoint);

            // add auth if provided, token > basic
            if (!token.isEmpty()) {
                _client.setAuthToken(token);
            } else if (!username.isEmpty() && !password.isEmpty()) {
                _client.setAuthBasic(username, password);
            }

            // add signature
            if (!secret.isEmpty()) {
                _client.setSignature(secret);
            }

            STATUS = SystemKeys.STATUS_READY;
        } else {
            // no endpoint
            STATUS = SystemKeys.STATUS_ERROR;
        }

        _log.debug("init status is " + String.valueOf(STATUS));
    }

    @Override
    public int getStatus() {
        return STATUS;
    }

    @Override
    public void addResource(String spaceId, String userId, Resource resource) throws ConsumerException {
        if (checkSpace(resource.getSpaceId()) && checkTags(resource.getTags())) {
            _log.debug("add resource " + resource.toString());
            try {
                if (resource.getProvider().equals(MinioProvider.ID)) {
                    // extract properties
                    String uri = resource.getUri();
                    String host = MinioUtils.getHost(uri);
                    int port = MinioUtils.getPort(uri);

                    String bucket = MinioUtils.getBucket(uri);

                    String accessKey = "";
                    String secretKey = "";

                    if (sendCredentials) {
                        accessKey = MinioUtils.getAccessKey(uri);
                        secretKey = MinioUtils.getSecretKey(uri);
                    }

                    // call endpoint via client
                    _client.call("add", resource.getSpaceId(), resource.getId(), "s3", host, port, bucket, accessKey,
                            secretKey);

                    _log.debug("resource added via endpoint " + endpoint);
                }
            } catch (WebhookException e) {
                _log.error("webhook error " + e.getMessage());
                throw new ConsumerException(e.getMessage());
            }
        }

    }

    @Override
    public void updateResource(String spaceId, String userId, Resource resource) throws ConsumerException {
        if (checkSpace(resource.getSpaceId())) {
            _log.debug("update resource " + resource.toString());
            try {
                if (resource.getProvider().equals(MinioProvider.ID)) {
                    // extract properties
                    String uri = resource.getUri();
                    String host = MinioUtils.getHost(uri);
                    int port = MinioUtils.getPort(uri);

                    String bucket = MinioUtils.getBucket(uri);

                    String accessKey = "";
                    String secretKey = "";

                    if (sendCredentials) {
                        accessKey = MinioUtils.getAccessKey(uri);
                        secretKey = MinioUtils.getSecretKey(uri);
                    }

                    if (checkTags(resource.getTags())) {
                        // call endpoint via client
                        _client.call("update", resource.getSpaceId(), resource.getId(), "s3", host, port, bucket,
                                accessKey, secretKey);
                        _log.debug("resource updated via endpoint " + endpoint);
                    } else {
                        // call endpoint via client
                        _client.call("delete", resource.getSpaceId(), resource.getId(), "s3", host, port, bucket,
                                accessKey,
                                secretKey);
                        _log.debug("resource deleted via endpoint " + endpoint);
                    }
                }
            } catch (WebhookException e) {
                _log.error("webhook error " + e.getMessage());
                throw new ConsumerException(e.getMessage());
            }

        }
    }

    @Override
    public void deleteResource(String spaceId, String userId, Resource resource) throws ConsumerException {
        if (checkSpace(resource.getSpaceId()) && checkTags(resource.getTags())) {
            _log.debug("delete resource " + resource.toString());
            try {
                if (resource.getProvider().equals(MinioProvider.ID)) {
                    // extract properties
                    String uri = resource.getUri();
                    String host = MinioUtils.getHost(uri);
                    int port = MinioUtils.getPort(uri);

                    String bucket = MinioUtils.getBucket(uri);

                    String accessKey = "";
                    String secretKey = "";

                    if (sendCredentials) {
                        accessKey = MinioUtils.getAccessKey(uri);
                        secretKey = MinioUtils.getSecretKey(uri);
                    }

                    _client.call("delete", resource.getSpaceId(), resource.getId(), "s3", host, port, bucket,
                            accessKey, secretKey);
                    _log.debug("resource deleted via endpoint " + endpoint);
                }
            } catch (WebhookException e) {
                _log.error("webhook error " + e.getMessage());
                throw new ConsumerException(e.getMessage());
            }
        }
    }

    @Override
    public void checkResource(String spaceId, String userId, Resource resource) throws ConsumerException {
        // not supported
    }

    /*
     * Helpers
     */

    public boolean checkTags(List<String> tags) {
        boolean ret = true;
        if (!this.tags.isEmpty() || !tags.isEmpty()) {
            ret = false;
            // look for at least one match
            for (String t : tags) {
                if (this.tags.contains(t)) {
                    ret = true;
                    break;
                }
            }
        }

        return ret;
    }

    public boolean checkSpace(String space) {
        if (spaceId == null) {
            return false;
        } else if (!this.spaceId.isEmpty()) {
            return spaceId.equals(space);
        } else {
            // if global space
            return true;
        }
    }
}