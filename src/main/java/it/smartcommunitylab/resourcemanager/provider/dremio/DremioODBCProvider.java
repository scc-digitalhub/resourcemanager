package it.smartcommunitylab.resourcemanager.provider.dremio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import it.smartcommunitylab.resourcemanager.SystemKeys;
import it.smartcommunitylab.resourcemanager.common.DuplicateNameException;
import it.smartcommunitylab.resourcemanager.common.InvalidNameException;
import it.smartcommunitylab.resourcemanager.common.NoSuchProviderException;
import it.smartcommunitylab.resourcemanager.common.ResourceProviderException;
import it.smartcommunitylab.resourcemanager.model.Resource;
import it.smartcommunitylab.resourcemanager.model.ResourceProvider;
import it.smartcommunitylab.resourcemanager.service.ResourceLocalService;
import it.smartcommunitylab.resourcemanager.util.OdbcUtil;

@Component
public class DremioODBCProvider extends ResourceProvider {
    private final static Logger _log = LoggerFactory.getLogger(DremioODBCProvider.class);

    public static final String TYPE = SystemKeys.TYPE_ODBC;
    public static final String ID = "dremioodbc";

    private static final String VALID_CHARS = "[a-zA-Z0-9]+";
    private static final String VIRTUAL_IDENTIFIER = "VIRTUAL_DATASET";

    @Value("${providers.dremio.enable}")
    private boolean ENABLED;

    @Value("${providers.dremio.properties}")
    private List<String> PROPERTIES;

    // dremio connection
    @Value("${providers.dremio.host}")
    private String HOST;

    @Value("${providers.dremio.port}")
    private int PORT;

    @Value("${providers.dremio.ssl}")
    private boolean SSL;

    @Value("${providers.dremio.username}")
    private String USERNAME;

    @Value("${providers.dremio.password}")
    private String PASSWORD;

    @Value("${providers.dremio.odbc.username}")
    private String ODBC_USERNAME;

    @Value("${providers.dremio.odbc.password}")
    private String ODBC_PASSWORD;

    @Value("${providers.dremio.interval}")
    private int INTERVAL;

    @Value("${providers.dremio.sync}")
    private boolean SYNC;

    // all resources will end in default scope
    // could maybe map dremioSpace -> scope?
    @Value("${scopes.default}")
    private String scopeId;

    private DremioClient _client;

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

        if (ENABLED) {
            String endpoint = (SSL ? "https://" : "http://") + HOST + ":" + Integer.toString(PORT);
            _client = new DremioClient(endpoint, USERNAME, PASSWORD);
            _log.info("init, scheduled every " + String.valueOf(INTERVAL) + "ms");
        }

    }

    @Override
    public int getStatus() {
        // NEVER return READY since we can not instantiate resources for users
        if (ENABLED) {
            return SystemKeys.STATUS_ENABLED;
        } else {
            return SystemKeys.STATUS_DISABLED;
        }
    }

    @Override
    public Resource createResource(String scopeId, String userId, String name, Map<String, Serializable> properties)
            throws ResourceProviderException, InvalidNameException, DuplicateNameException {
        throw new ResourceProviderException("not supported");
    }

    @Override
    public void updateResource(Resource resource) throws ResourceProviderException {
    }

    @Override
    public void deleteResource(Resource resource) throws ResourceProviderException {
    }

    @Override
    public void checkResource(Resource resource) throws ResourceProviderException {
    }

    /*
     * Scheduler
     */

    @Scheduled(initialDelay = 10000, fixedRateString = "${providers.dremio.interval}")
    public void reflectDatasets() {
        if (ENABLED) {
            try {
                _log.debug("reflectDatasets execution");

                // fetch resources
                Set<String> keys = new HashSet<>();
                Set<String> virtual = new HashSet<>();
                List<Resource> list = resourceLocalService.listByProvider(ID);
                List<Resource> resources = new ArrayList<>();
                for (Resource r : list) {
                    // need to explicitely fetch to decrypt URI
                    Resource res = resourceLocalService.get(r.getId());
                    keys.add(OdbcUtil.getDatabase(res.getUri()));
                    resources.add(res);
                }

                _log.debug("reflectDatasets has found " + String.valueOf(keys.size()) + " resources in DB");
                _log.trace("keys " + keys.toString());

                JSONArray datasets = _client.listDatasets();

                _log.debug("reflectDatasets has found " + String.valueOf(datasets.length()) + " datasets in JSON");

                for (int i = 0; i < datasets.length(); i++) {
                    JSONObject dataset = datasets.getJSONObject(i);

                    try {
                        // read only virtual
                        if (VIRTUAL_IDENTIFIER.equals(dataset.optString("datasetType", ""))) {
                            _log.debug("found virtual dataset");

                            JSONArray fullPath = dataset.getJSONArray("fullPath");
                            String[] path = new String[fullPath.length()];

                            for (int j = 0; j < path.length; j++) {
                                path[j] = fullPath.getString(j);
                            }

                            // build key
                            // TODO validate and properly encode/escape
                            String key = String.join(".", path);

                            _log.debug("virtual dataset key set as " + key);

                            // save ref
                            virtual.add(key);

                            // check if already exists
                            if (!keys.contains(key)) {
                                // add
                                _log.debug("add virtual dataset " + key);
                                Resource res = addResource(key);
                                _log.debug("added resource " + res.getId());

                                keys.add(key);

                            }

                        }
                    } catch (Exception e) {
                        // skip
                        e.printStackTrace();
                        _log.error("error " + e.getMessage());
                    }
                }

                if (SYNC) {
                    _log.debug("clear orphan resources");

                    // remove missing datasets
                    for (Resource res : resources) {
                        String key = OdbcUtil.getDatabase(res.getUri());
                        _log.debug("check resource with key " + key);

                        // has to exists in virtual keys
                        if (!virtual.contains(key)) {
                            // remove
                            _log.debug("remove resource with id " + res.getId());
                            resourceLocalService.delete(res.getId());
                        }
                    }

                }

            } catch (Exception ex) {
                ex.printStackTrace();
                _log.error("error " + ex.getMessage());
            }
        }
    }

    /*
     * Helpers
     */

    public Resource addResource(String name) throws NoSuchProviderException, ResourceProviderException {
        // prepare data
        Map<String, Serializable> properties = new HashMap<>();
        List<String> tags = new ArrayList<>();
        String endpoint = HOST + ":31010";

        // pack uri
        String uri = OdbcUtil.encodeURI("dremioodbc", endpoint, name, ODBC_USERNAME, ODBC_PASSWORD);

        // use our id as username
        // TODO replace
        String userId = ID;
        // register an unmanaged resource
        return resourceLocalService.add(scopeId, userId, TYPE, ID, uri, properties, tags);

    }

}
