package it.smartcommunitylab.resourcemanager.provider.minio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import io.minio.errors.MinioException;
import it.smartcommunitylab.resourcemanager.SystemKeys;
import it.smartcommunitylab.resourcemanager.common.DuplicateNameException;
import it.smartcommunitylab.resourcemanager.common.InvalidNameException;
import it.smartcommunitylab.resourcemanager.common.ResourceProviderException;
import it.smartcommunitylab.resourcemanager.model.Resource;
import it.smartcommunitylab.resourcemanager.model.ResourceProvider;
import it.smartcommunitylab.resourcemanager.service.ResourceLocalService;

@Component
public class MinioProvider extends ResourceProvider {
    private final static Logger _log = LoggerFactory.getLogger(MinioProvider.class);

    public static final String TYPE = SystemKeys.TYPE_OBJECT;
    public static final String ID = "minio";

    private static final String VALID_CHARS = "[a-zA-Z0-9]+";

    private int STATUS;

    @Value("${providers.minio.enable}")
    private boolean ENABLED;

    @Value("${providers.minio.checkOnStart}")
    private boolean STARTUP_CHECK;

    @Value("${providers.minio.properties}")
    private List<String> PROPERTIES;

    // minio play connection
    @Value("${providers.minio.host}")
    private String HOST;

    @Value("${providers.minio.port}")
    private int PORT;

    @Value("${providers.minio.ssl}")
    private boolean SSL;

    @Value("${providers.minio.accessKey}")
    private String ACCESS_KEY;

    @Value("${providers.minio.secretKey}")
    private String SECRET_KEY;

//    @Value("${providers.minio.userAccessKey}")
//    private String USER_ACCESS_KEY;
//
//    @Value("${providers.minio.userSecretKey}")
//    private String USER_SECRET_KEY;

    @Value("${providers.minio.clearOnDelete}")
    private boolean CLEAR_ON_DELETE;

    @Value("${providers.minio.useSpacePolicy}")
    private boolean SPACE_POLICIES;

    private MinioS3Client _client;

    @Autowired
    @Lazy
    private ResourceLocalService resourceLocalService;

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
            _client = new MinioS3Client(HOST, PORT, SSL, ACCESS_KEY, SECRET_KEY);

            if (STARTUP_CHECK) {
                // check minio status
                // TODO implement status check
                // via REST api on client

                if (_client.ping()) {
                    STATUS = SystemKeys.STATUS_READY;
                } else {
                    STATUS = SystemKeys.STATUS_ERROR;
                }
            } else {
                STATUS = SystemKeys.STATUS_READY;
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
                sb.append(spaceId.replaceAll("[^A-Za-z0-9]", "")).append("-");
                sb.append(userId.replaceAll("[^A-Za-z0-9]", "")).append("-");
                sb.append(name);

                name = sb.toString();

                // check duplicate for spaced name
                if (_client.hasBucket(name)) {
                    throw new DuplicateNameException();
                }
            } else {
                // generate id with limited tries
                name = generateId(spaceId, userId);
                int retry = 0;
                boolean exists = _client.hasBucket(name);
                while (exists && retry < 8) {
                    name = generateId(spaceId, userId);
                    exists = _client.hasBucket(name);
                    retry++;
                }

                if (exists) {
                    throw new ResourceProviderException("error creating bucket");
                }
            }

            _log.info("create bucket " + name + " with space " + spaceId + " for user " + userId);

            // create bucket
            _client.createBucket(name);

            _log.info("create policies for bucket " + name);

            // create policies for bucket
            String bucketPolicyRW = _client.createPolicy(
                    name + "_" + MinioS3Client.POLICY_RW,
                    name, MinioS3Client.POLICY_RW);
            String bucketPolicyRO = _client.createPolicy(
                    name + "_" + MinioS3Client.POLICY_RO,
                    name, MinioS3Client.POLICY_RO);

            if (SPACE_POLICIES) {
                _log.info("update policies for space " + spaceId);

                // update rw policy for space
                String spacePolicyRW = spaceId + "_" + MinioS3Client.POLICY_RW;
                String spacePolicyRO = spaceId + "_" + MinioS3Client.POLICY_RO;

                // fetch list of all buckets for current space
                // name is bucket name
                List<String> bucketNames = resourceLocalService.listByProviderAndSpaceId(ID, spaceId).stream()
                        .map(r -> r.getName()).collect(Collectors.toList());

                // append new bucket
                bucketNames.add(name);

                // update policies
                _client.createOrUpdatePolicy(spacePolicyRW, bucketNames, MinioS3Client.POLICY_RW);
                _client.createOrUpdatePolicy(spacePolicyRO, bucketNames, MinioS3Client.POLICY_RO);

            }

            // create user
            // TODO check listUsers to avoid duplicates
            String userAccessKey = RandomStringUtils.randomAlphanumeric(20).toUpperCase();

            String userSecretKey = RandomStringUtils.randomAlphanumeric(20)
                    + RandomStringUtils.randomAlphanumeric(12) // + "+"
                    + RandomStringUtils.randomAlphanumeric(4);

            _client.createUser(userAccessKey, userSecretKey, bucketPolicyRW);

            // generate uri
            String endpoint = HOST + ":" + String.valueOf(PORT);
            String uri = MinioUtils.encodeURI(endpoint, name, userAccessKey, userSecretKey, SSL);

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
                + " with space " + resource.getSpaceId()
                + " for user " + resource.getUserId());

        // extract info from resource
//        String bucket = MinioUtils.getBucket(resource.getUri());
        String bucket = resource.getName();
        String userAccessKey = MinioUtils.getAccessKey(resource.getUri());
//        String userSecretKey = MinioUtils.getSecretKey(resource.getUri());

        try {

            // delete user
            try {
                _client.removeUser(userAccessKey);
                _log.info("removed user " + userAccessKey);
            } catch (MinioException mex) {
                _log.error("remove user " + userAccessKey + " error " + mex.getMessage());
            }
            // drop bucket policies
            try {
                _client.removePolicy(bucket + "_" + MinioS3Client.POLICY_RW);
                _log.info("removed policy " + bucket + "_" + MinioS3Client.POLICY_RW);

            } catch (MinioException mex) {
                _log.error("remove policy " + bucket + "_" + MinioS3Client.POLICY_RW + " error " + mex.getMessage());
            }

            try {
                _client.removePolicy(bucket + "_" + MinioS3Client.POLICY_RO);
                _log.info("removed policy " + bucket + "_" + MinioS3Client.POLICY_RO);
            } catch (MinioException mex) {
                _log.error("remove policy " + bucket + "_" + MinioS3Client.POLICY_RO + " error " + mex.getMessage());
            }

            if (SPACE_POLICIES) {
                String spaceId = resource.getSpaceId();
                _log.info("update policies for space " + spaceId);

                // update rw policy for space
                String spacePolicyRW = spaceId + "_" + MinioS3Client.POLICY_RW;
                String spacePolicyRO = spaceId + "_" + MinioS3Client.POLICY_RO;

                // fetch list of all buckets for current space
                // name is bucket name
                List<String> bucketNames = resourceLocalService.listByProviderAndSpaceId(ID, spaceId).stream()
                        .map(r -> r.getName()).collect(Collectors.toList());

                // remove current bucket
                bucketNames.remove(bucket);

                // update policies
                _client.createOrUpdatePolicy(spacePolicyRW, bucketNames, MinioS3Client.POLICY_RW);
                _client.createOrUpdatePolicy(spacePolicyRO, bucketNames, MinioS3Client.POLICY_RO);
            }

            // delete bucket with drop all objects?
            _log.info("drop bucket " + bucket + " with clear:" + String.valueOf(CLEAR_ON_DELETE));
            _client.deleteBucket(bucket, CLEAR_ON_DELETE);

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
    private String generateId(String spaceId, String userId) {
        // build id from context plus random string
        StringBuilder sb = new StringBuilder();
        // cleanup space and userId to alphanum - will strip non ascii
        // use only - as separator to obtain simpler urls
        sb.append(spaceId.replaceAll("[^A-Za-z0-9]", "")).append("-");
//        sb.append(userId.replaceAll("[^A-Za-z0-9]", "")).append("-");

        // random suffix length 5
        sb.append(RandomStringUtils.randomAlphanumeric(6));

        // ensure lowercase
        return sb.toString().toLowerCase();
    }

}
