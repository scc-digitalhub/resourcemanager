package it.smartcommunitylab.resourcemanager.consumer.dremio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.smartcommunitylab.resourcemanager.SystemKeys;
import it.smartcommunitylab.resourcemanager.common.ConsumerException;
import it.smartcommunitylab.resourcemanager.model.Consumer;
import it.smartcommunitylab.resourcemanager.model.Registration;
import it.smartcommunitylab.resourcemanager.model.Resource;
import it.smartcommunitylab.resourcemanager.provider.minio.MinioProvider;
import it.smartcommunitylab.resourcemanager.provider.minio.MinioUtils;
import it.smartcommunitylab.resourcemanager.util.SqlUtil;

public class DremioS3Consumer extends Consumer {

    private final static Logger _log = LoggerFactory.getLogger(DremioS3Consumer.class);

    public static final String TYPE = SystemKeys.TYPE_OBJECT;
    public static final String ID = "dremios3";

    // sqlpad connection
    private String endpoint;
    private String username;
    private String password;
    private boolean version4;

    private int STATUS;

    private Registration registration;

    // filters
    private String spaceId;
    private List<String> tags;

    private DremioClient _client;

    public DremioS3Consumer() {
        endpoint = "";
        username = "";
        password = "";
        spaceId = "";
        tags = new ArrayList<>();
    }

    public DremioS3Consumer(Map<String, Serializable> properties) {
        this();
        _properties = properties;
    }

    public DremioS3Consumer(Registration reg) {
        this();
        registration = reg;
        _properties = reg.getPropertiesMap();
        spaceId = reg.getSpaceId();
        tags = reg.getTags();
    }

    // dremio properties
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
        // build access url from endpoint
        return endpoint;
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
            if (_properties.containsKey("endpoint")
                    && _properties.containsKey("username")
                    && _properties.containsKey("password")) {

                endpoint = _properties.get("endpoint").toString();
                username = _properties.get("username").toString();
                password = _properties.get("password").toString();
            }

            if (_properties.containsKey("version4")) {
                version4 = Boolean.parseBoolean(_properties.get("version4").toString());
            } else {
                version4 = true;
            }
        }

        if (!endpoint.isEmpty() && !username.isEmpty() && !password.isEmpty()) {
            _client = new DremioClient(endpoint, username, password, version4);
            // DISABLED check - TODO implement async check and recovery
//            // test via ping
//            try {
//                _client.ping();
//            } catch (DremioException e) {
//                _log.error("ping error:" + e.getMessage());
//                STATUS = SystemKeys.STATUS_ERROR;
//            }

            STATUS = SystemKeys.STATUS_READY;
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
                // fetch type from supported
                String type = getType(resource.getProvider());
                if (!type.isEmpty()) {
                    // supported
                    String provider = resource.getProvider();
                    String uri = resource.getUri();
                    String host = extractURI(provider, uri, "host");
                    int port = Integer.parseInt(extractURI(provider, uri, "port"));
                    boolean ssl = Boolean.parseBoolean(extractURI(provider, uri, "ssl"));
                    String uname = extractURI(provider, uri, "username");
                    String passw = extractURI(provider, uri, "password");
                    String database = extractURI(provider, uri, "database");
                    String name = type.toLowerCase() + "_" + database;

                    name = _client.addSource(type, name, host, port, ssl, database, uname, passw);
                    _log.debug("created source " + name);
                }
            } catch (DremioException e) {
                _log.error("dremio error " + e.getMessage());
                throw new ConsumerException(e.getMessage());
            }
        }
    }

    @Override
    public void updateResource(String spaceId, String userId, Resource resource) throws ConsumerException {
        if (checkSpace(resource.getSpaceId())) {
            _log.debug("update resource " + resource.toString());
            try {
                // fetch type from supported
                String type = getType(resource.getProvider());
                if (!type.isEmpty()) {
                    // supported
                    String provider = resource.getProvider();
                    String uri = resource.getUri();
                    String host = extractURI(provider, uri, "host");
                    int port = Integer.parseInt(extractURI(provider, uri, "port"));
                    boolean ssl = Boolean.parseBoolean(extractURI(provider, uri, "ssl"));
                    String uname = extractURI(provider, uri, "username");
                    String passw = extractURI(provider, uri, "password");
                    String database = extractURI(provider, uri, "database");
                    String name = type.toLowerCase() + "_" + database;

                    if (checkTags(resource.getTags())) {
                        // matches, update or create via client
                        if (_client.hasSource(name)) {
                            // exists, update
                            name = _client.updateSource(type, name, host, port, ssl, database, uname, passw);
                            _log.debug("updated source " + name);
                        } else {
                            // create as new
                            name = _client.addSource(type, name, host, port, ssl, database, uname, passw);
                            _log.debug("created source " + name);
                        }
                    } else {
                        if (_client.hasSource(name)) {
                            // remove previously existing resource
                            _client.deleteSource(name);
                            _log.debug("deleted source " + name);
                        }
                    }
                }
            } catch (DremioException e) {
                _log.error("dremio error " + e.getMessage());
                throw new ConsumerException(e.getMessage());
            }
        }
    }

    @Override
    public void deleteResource(String spaceId, String userId, Resource resource) throws ConsumerException {
        if (checkSpace(resource.getSpaceId()) && checkTags(resource.getTags())) {
            _log.debug("delete resource " + resource.toString());
            try {
                // fetch type from supported
                String type = getType(resource.getProvider());
                if (!type.isEmpty()) {
                    // supported
                    String provider = resource.getProvider();
                    String uri = resource.getUri();
                    String database = extractURI(provider, uri, "database");
                    String name = type.toLowerCase() + "_" + database;

                    _client.deleteSource(name);
                    _log.debug("deleted if existing source " + name);

                }
            } catch (DremioException e) {
                _log.error("dremio error " + e.getMessage());
                throw new ConsumerException(e.getMessage());
            }
        }
    }

    @Override
    public void checkResource(String spaceId, String userId, Resource resource) throws ConsumerException {
        if (checkSpace(resource.getSpaceId()) && checkTags(resource.getTags())) {
            _log.debug("check resource " + resource.toString());
            try {
                // fetch type from supported
                String type = getType(resource.getProvider());
                if (!type.isEmpty()) {
                    // supported
                    String uri = resource.getUri();
                    String database = SqlUtil.getDatabase(uri);
                    String name = type.toLowerCase() + "_" + database;

                    boolean exists = _client.hasSource(name);

                    if (exists) {
                        _log.debug("check ok source " + name);
                    }
                }
            } catch (DremioException e) {
                _log.error("dremio error " + e.getMessage());
                throw new ConsumerException(e.getMessage());
            }
        }
    }

    /*
     * Helpers
     */
    public String getType(String provider) {
        String type = "";
        switch (provider) {
        case MinioProvider.ID:
            type = "S3";
            break;
        }
        return type;
    }

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

    public String extractURI(String provider, String uri, String property) {
        String value = "";

        if (provider.equals(MinioProvider.ID)) {
            switch (property) {
            case "host":
                value = MinioUtils.getHost(uri);
                break;
            case "port":
                value = Integer.toString(MinioUtils.getPort(uri));
                break;
            case "ssl":
                value = Boolean.toString(MinioUtils.getSsl(uri));
                break;
            case "username":
                value = MinioUtils.getAccessKey(uri);
                break;
            case "password":
                value = MinioUtils.getSecretKey(uri);
                break;
            case "database":
                value = MinioUtils.getBucket(uri);
                break;
            }
        }

        return value;
    }

}
