package it.smartcommunitylab.resourcemanager.provider.cockroachdb;

import java.io.Serializable;
import java.sql.SQLException;
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

@Component
public class CockroachDBProvider extends ResourceProvider {
    private final static Logger _log = LoggerFactory.getLogger(CockroachDBProvider.class);

    public static final String TYPE = SystemKeys.TYPE_SQL;
    public static final String ID = "cockroachDB";

    private static final String VALID_CHARS = "[a-zA-Z0-9]+";

    private int STATUS;

    @Value("${providers.cockroachdb.enable}")
    private boolean ENABLED;

    @Value("${providers.cockroachdb.properties}")
    private List<String> PROPERTIES;

    // CockroachDB connection
    @Value("${providers.cockroachdb.host}")
    private String HOST;

    @Value("${providers.cockroachdb.port}")
    private int PORT;

    @Value("${providers.cockroachdb.ssl}")
    private boolean SSL;

    @Value("${providers.cockroachdb.username}")
    private String USERNAME;

    @Value("${providers.cockroachdb.password}")
    private String PASSWORD;

    @Value("${providers.cockroachdb.insecure}")
    private boolean INSECURE;

    private CockroachDBClient _client;

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
            _client = new CockroachDBClient(HOST, PORT, SSL, USERNAME, PASSWORD);
            // check CockroachDB availability

            if (_client.ping()) {
                STATUS = SystemKeys.STATUS_READY;
            } else {
                STATUS = SystemKeys.STATUS_ERROR;
            }

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
            _client.createDatabase(name);

            // generate uri
            String endpoint = HOST + ":" + String.valueOf(PORT);
            String uri = SqlUtil.encodeURI("cockroachdb", endpoint, name);

            // if cluster in insecure mode user creation is disabled!
            if (!INSECURE) {
                // create username = dbname
                String username = name;
                String password = RandomStringUtils.randomAlphanumeric(10);

                _log.info("create user " + username + " for database " + name);

                _client.createUser(name, username, password);

                // generate uri
                uri = SqlUtil.encodeURI("cockroachdb", endpoint, name, username, password);
            }

            // update res
            res.setName(name);
            res.setUri(uri);

            return res;
        } catch (SQLException sex) {
            _log.error(sex.getMessage());
            throw new ResourceProviderException("sql error");
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
            if (!INSECURE) {
                if (!USERNAME.equals(username) && !username.isEmpty()) {
                    // delete user first
                    _log.info("drop user " + username + " for database " + database);
                    _client.deleteUser(database, username);
                }
            }

            if (!database.isEmpty()) {
                // delete database
                _log.info("drop database " + database);
                _client.deleteDatabase(database);
            }
        } catch (SQLException sex) {
            _log.error(sex.getMessage());
            throw new ResourceProviderException("sql error");
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
        // use only _ as separator otherwise postgres will complain
        sb.append(spaceId.replaceAll("[^A-Za-z0-9]", "")).append("_");
//        sb.append(userId.replaceAll("[^A-Za-z0-9]", "")).append("_");

        // random suffix length 5
        sb.append(RandomStringUtils.randomAlphanumeric(6));

        // ensure lowercase
        return sb.toString().toLowerCase();
    }
}
