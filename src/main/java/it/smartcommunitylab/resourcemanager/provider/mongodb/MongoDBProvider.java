package it.smartcommunitylab.resourcemanager.provider.mongodb;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.smartcommunitylab.resourcemanager.SystemKeys;
import it.smartcommunitylab.resourcemanager.common.DuplicateNameException;
import it.smartcommunitylab.resourcemanager.common.InvalidNameException;
import it.smartcommunitylab.resourcemanager.common.ResourceProviderException;
import it.smartcommunitylab.resourcemanager.model.Resource;
import it.smartcommunitylab.resourcemanager.model.ResourceProvider;
import it.smartcommunitylab.resourcemanager.util.SqlUtil;
import it.smartcommunitylab.resourcemanager.util.StringUtils;

@Component
public class MongoDBProvider extends ResourceProvider {
    private final static Logger _log = LoggerFactory.getLogger(MongoDBProvider.class);

    public static final String TYPE = SystemKeys.TYPE_NOSQL;
    public static final String ID = "mongodb";

    private static final String VALID_CHARS = "[a-zA-Z0-9]+";

    private int STATUS;

    @Value("${providers.mongodb.enable}")
    private boolean ENABLED;

    @Value("${providers.mongodb.properties}")
    private List<String> PROPERTIES;

    // mongodb connection
    @Value("${providers.mongodb.host}")
    private String HOST;

    @Value("${providers.mongodb.port}")
    private int PORT;

    @Value("${providers.mongodb.ssl}")
    private boolean SSL;

    @Value("${providers.mongodb.username}")
    private String USERNAME;

    @Value("${providers.mongodb.password}")
    private String PASSWORD;

    private MongoDBClient _client;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Set<String> listProperties() {
        return new HashSet<String>(PROPERTIES);
    }

    /*
     * Init method - POST constructor since spring injects properties *after
     * creation*
     */
    @PostConstruct
    public void init() {
        _log.info("enabled " + String.valueOf(ENABLED));
        STATUS = SystemKeys.STATUS_DISABLED;

        if (ENABLED) {
            _client = new MongoDBClient(HOST, PORT, SSL, USERNAME, PASSWORD);
            // do not check connection now
            STATUS = SystemKeys.STATUS_READY;
        }

        _log.info("init status " + String.valueOf(STATUS));
    }

    @Override
    public int getStatus() {
        return STATUS;
    }

    @Override
    public Resource createResource(String spaceId, String userId, String name, Map<String, Serializable> properties)
            throws ResourceProviderException, InvalidNameException, DuplicateNameException {
        Resource res = new Resource();
        res.setType(TYPE);
        res.setProvider(ID);
        res.setPropertiesMap(properties);

        try {
            if (!name.isEmpty()) {
                // validate name
                if (!name.matches(VALID_CHARS)) {
                    throw new InvalidNameException();
                }

                // build spaced name
                StringBuilder sb = new StringBuilder();
                sb.append(spaceId.replaceAll("[^A-Za-z0-9]", "")).append("_");
                sb.append(userId.replaceAll("[^A-Za-z0-9]", "")).append("_");
                sb.append(name);

                name = sb.toString();

                // check duplicate for spaced name
                if (_client.hasDatabase(name)) {
                    throw new DuplicateNameException();
                }
            } else {
                // generate id with limited tries
                name = generateId(spaceId, userId);
                int retry = 0;
                boolean exists = _client.hasDatabase(name);
                while (exists && retry < 8) {
                    name = generateId(spaceId, userId);
                    exists = _client.hasDatabase(name);
                    retry++;
                }

                if (exists) {
                    throw new ResourceProviderException("error creating database");
                }
            }

            _log.info("create database " + name + " with space " + spaceId + " for user " + userId);

            // create database
            // will create a collection "rm" with space + userId
            _client.createDatabase(name);

            // create username = dbname
            // will also create database
            String username = name;
            String password = RandomStringUtils.randomAlphanumeric(10);

            _log.info("create user " + username + " for database " + name);

            _client.createUser(name, username, password, MongoDBClient.ROLE_RW);

            // generate uri
            String endpoint = HOST + ":" + String.valueOf(PORT);
            String uri = SqlUtil.encodeURI("mongodb", endpoint, name, username, password);

            // update res
            res.setName(name);
            res.setUri(uri);

            return res;
        } catch (Exception ex) {
            _log.error(ex.getMessage());
            throw new ResourceProviderException("mongodb error");
        }
    }

    @Override
    public void updateResource(Resource resource) throws ResourceProviderException {
        // TODO

    }

    @Override
    public void deleteResource(Resource resource) throws ResourceProviderException {

        _log.info("delete resource " + String.valueOf(resource.getId())
                + " with space " + resource.getSpaceId()
                + " for user " + resource.getUserId());

        // extract info from resource
        String database = SqlUtil.getDatabase(resource.getUri());
        String username = SqlUtil.getUsername(resource.getUri());

        try {
            if (!USERNAME.equals(username) && !username.isEmpty()) {
                // delete user first
                _log.info("drop user " + username + " for database " + database);
                _client.deleteUser(database, username);
            }

            if (!database.isEmpty()) {
                // delete database
                _log.info("drop database " + database);
                _client.deleteDatabase(database);
            }
        } catch (Exception ex) {
            _log.error(ex.getMessage());
            throw new ResourceProviderException("mongodb error");
        }
    }

    @Override
    public void checkResource(Resource resource) throws ResourceProviderException {
        // TODO
    }

    /*
     * Helpers
     */
    private String generateId(String spaceId, String userId) {
        // build id from context plus random string
        StringBuilder sb = new StringBuilder();
        // cleanup space and userId to alphanum - will strip non ascii
        // use only _ as separator
        sb.append(StringUtils.shorten(spaceId.replaceAll("[^A-Za-z0-9]", ""), 10).toLowerCase()).append("_");
        sb.append(StringUtils.shorten(userId.replaceAll("[^A-Za-z0-9]", ""), 12).toLowerCase()).append("_");

        // random suffix length 6
        sb.append(RandomStringUtils.randomAlphanumeric(6));

        // ensure lowercase
        return sb.toString().toLowerCase();
    }
}