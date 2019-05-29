package it.smartcommunitylab.resourcemanager.provider.minio;

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

import io.minio.errors.MinioException;
import it.smartcommunitylab.resourcemanager.SystemKeys;
import it.smartcommunitylab.resourcemanager.common.DuplicateNameException;
import it.smartcommunitylab.resourcemanager.common.InvalidNameException;
import it.smartcommunitylab.resourcemanager.common.ResourceProviderException;
import it.smartcommunitylab.resourcemanager.model.Resource;
import it.smartcommunitylab.resourcemanager.model.ResourceProvider;

@Component
public class MinioProvider extends ResourceProvider {
    private final static Logger _log = LoggerFactory.getLogger(MinioProvider.class);

    public static final String TYPE = SystemKeys.TYPE_OBJECT;
    public static final String ID = "minio";

    private static final String VALID_CHARS = "[a-zA-Z0-9]+";

    private int STATUS;

    @Value("${providers.minio.enable}")
    private boolean enabled;

    @Value("${providers.minio.properties}")
    private List<String> properties;

    // minio play connection
    @Value("${providers.minio.host}")
    private String host;

    @Value("${providers.minio.port}")
    private int port;

    @Value("${providers.minio.ssl}")
    private boolean ssl;

    @Value("${providers.minio.accessKey}")
    private String accessKey;

    @Value("${providers.minio.secretKey}")
    private String secretKey;

    @Value("${providers.minio.userAccessKey}")
    private String userAccessKey;

    @Value("${providers.minio.userSecretKey}")
    private String userSecretKey;

    @Value("${providers.minio.clearOnDelete}")
    private boolean clearOnDelete;

    private MinioS3Client _client;

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
        return new HashSet<String>(properties);
    }

    /*
     * Init method - POST constructor since spring injects properties *after
     * creation*
     */
    @PostConstruct
    public void init() {
        _log.info("enabled " + String.valueOf(enabled));
        STATUS = SystemKeys.STATUS_DISABLED;

        if (enabled) {
            _client = new MinioS3Client(host, port, ssl, accessKey, secretKey);
            // check minio status
            // TODO implement status check
            // via REST api on client

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
    public Resource createResource(String scopeId, String userId, String name, Map<String, Serializable> properties)
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

                // build scoped name
                StringBuilder sb = new StringBuilder();
                sb.append(scopeId.replaceAll("[^A-Za-z0-9]", "")).append("-");
                sb.append(userId.replaceAll("[^A-Za-z0-9]", "")).append("-");
                sb.append(name);

                name = sb.toString();

                // check duplicate for scoped name
                if (_client.hasBucket(name)) {
                    throw new DuplicateNameException();
                }
            } else {
                // generate id with limited tries
                name = generateId(scopeId, userId);
                int retry = 0;
                boolean exists = _client.hasBucket(name);
                while (exists && retry < 8) {
                    name = generateId(scopeId, userId);
                    exists = _client.hasBucket(name);
                    retry++;
                }

                if (exists) {
                    throw new ResourceProviderException("error creating bucket");
                }
            }

            _log.info("create bucket " + name + " with scope " + scopeId + " for user " + userId);

            // create database
            _client.createBucket(name);

            // create user - TODO
            // unsupported now, use fixed user credentials
            // set in properties

            // generate uri
            String endpoint = host + ":" + String.valueOf(port);
            String uri = MinioUtils.encodeURI(endpoint, name, userAccessKey, userSecretKey);

            // update res
            res.setName(name);
            res.setUri(uri);

            return res;
        } catch (MinioException mex) {
            _log.error(mex.getMessage());
            throw new ResourceProviderException("minio error");
        }
    }

    @Override
    public void updateResource(Resource resource) throws ResourceProviderException {
        // TODO

    }

    @Override
    public void deleteResource(Resource resource) throws ResourceProviderException {

        _log.info("delete resource " + String.valueOf(resource.getId())
                + " with scope " + resource.getScopeId()
                + " for user " + resource.getUserId());

        // extract info from resource
        String bucket = MinioUtils.getBucket(resource.getUri());

        try {
            // delete bucket with drop all objects?
            _log.info("drop bucket " + bucket + " with clear:" + String.valueOf(clearOnDelete));
            _client.deleteBucket(bucket, clearOnDelete);

            // delete user - TODO
            // only if dynamic 1user-per-bucket implemented
            // disabled now with fixed user credentials

        } catch (MinioException mex) {
            _log.error(mex.getMessage());
            throw new ResourceProviderException("minio error");
        }
    }

    @Override
    public void checkResource(Resource resource) throws ResourceProviderException {
        // TODO
    }

    /*
     * Helpers
     */
    private String generateId(String scopeId, String userId) {
        // build id from context plus random string
        StringBuilder sb = new StringBuilder();
        // cleanup scope and userId to alphanum - will strip non ascii
        // use only - as separator to obtain simpler urls
        sb.append(scopeId.replaceAll("[^A-Za-z0-9]", "")).append("-");
        sb.append(userId.replaceAll("[^A-Za-z0-9]", "")).append("-");

        // random suffix length 5
        sb.append(RandomStringUtils.randomAlphanumeric(5));

        // ensure lowercase
        return sb.toString().toLowerCase();
    }

}
