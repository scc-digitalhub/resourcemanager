package it.smartcommunitylab.resourcemanager.consumer.dss;

import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class DSSRestClient {
    private final static Logger _log = LoggerFactory.getLogger(DSSRestClient.class);

    private String ENDPOINT;
    private String USERNAME;
    private String PASSWORD;
    private String TENANT;

    private final static String API = "/rest/";

    public DSSRestClient(String endpoint, String tenant, String username, String password) {
        super();
        ENDPOINT = endpoint;
        TENANT = tenant;
        USERNAME = username;
        PASSWORD = password;

    }

    public JSONArray listDataSources() throws DSSException {
        try {
            JSONArray datasets = null;

            RestTemplate template = new RestTemplate();
            HttpHeaders headers = headers(USERNAME, PASSWORD);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // fetch response as String because it may not match the json schema
            ResponseEntity<String> response = template.exchange(ENDPOINT + API + TENANT + "/listDataSource",
                    HttpMethod.GET,
                    entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                // fetch JSON array
                datasets = new JSONArray(response.getBody());
            } else {
                throw new DSSException("response error code " + response.getStatusCode());
            }

            return datasets;
        } catch (RestClientException rex) {
            throw new DSSException("rest error " + rex.getMessage());
        }
    }

    public boolean hasSource(String name) throws DSSException {
        boolean exists = false;

        JSONArray sources = listDataSources();
        for (int i = 0; i < sources.length(); i++) {
            JSONObject source = sources.getJSONObject(i);
            if (name.equals(source.optString("name", ""))) {
                exists = true;
                break;
            }
        }

        return exists;
    }

    public String addSource(
            String type,
            String name,
            String host, int port,
            String database,
            String username, String password)
            throws DSSException {
        try {
            JSONObject json = new JSONObject();
            json.put("name", name);
            json.put("type", type);

            JSONObject config = getMetaInfo(type, host, port, database, username, password);
            if (config == null) {
                throw new DSSException("error generating client configuration");
            }

            json.put("config", config);

            RestTemplate template = new RestTemplate();
            HttpHeaders headers = headers(USERNAME, PASSWORD);

            _log.trace("add source json " + json.toString());

            HttpEntity<String> entity = new HttpEntity<>(json.toString(), headers);

            // fetch response as String because it may not match the json schema
            ResponseEntity<String> response = template.exchange(ENDPOINT + API + TENANT + "/addDataSource/" + name,
                    HttpMethod.POST,
                    entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return name;
            } else {
                throw new DSSException("response error code " + response.getStatusCode());
            }
        } catch (JSONException jex) {
            throw new DSSException("json error " + jex.getMessage());
        } catch (RestClientException rex) {
            throw new DSSException("rest error " + rex.getMessage());
        }

    }

    public String updateSource(
            String type,
            String name,
            String host, int port,
            String database,
            String username, String password)
            throws DSSException {
        // TODO
        return name;

    }

    public void deleteSource(String name) throws DSSException {
        try {
            RestTemplate template = new RestTemplate();
            HttpHeaders headers = headers(USERNAME, PASSWORD);

            JSONObject json = new JSONObject();
            HttpEntity<String> entity = new HttpEntity<>(json.toString(), headers);

            String path = ""; // TODO fetch id from DSS

            // fetch response as String because it should be empty
            ResponseEntity<String> response = template.exchange(
                    ENDPOINT + API + TENANT + "/deleteDataSource/" + path, HttpMethod.DELETE, entity,
                    String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                // success
            } else {
                throw new DSSException("response error code " + response.getStatusCode());
            }
        } catch (RestClientException rex) {
            rex.printStackTrace();
            throw new DSSException("rest error " + rex.getMessage());
        }
    }

    /*
     * Helpers
     */

    private HttpHeaders headers(String username, String password) throws RestClientException {

        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(
                auth.getBytes(Charset.forName("UTF-8")));
        String authHeader = "Basic " + new String(encodedAuth);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        return headers;
    }

    private JSONObject getMetaInfo(String type,
            String host, int port,
            String database,
            String username, String password) {

        if (type.equals("POSTGRES")) {
            return getPostgresConfiguration(host, port, database, username, password);
        }

        if (type.equals("MYSQL")) {
            return getMySqlConfiguration(host, port, database, username, password);
        }

        if (type.equals("MONGO")) {
            return getMongoConfiguration(host, port, database, username, password);
        }

        return null;

    }

    private JSONObject getPostgresConfiguration(
            String host, int port,
            String database,
            String username, String password) {
        JSONObject json = new JSONObject();
        // TODO

        return json;

    }

    private JSONObject getMySqlConfiguration(
            String host, int port,
            String database,
            String username, String password) {
        JSONObject json = new JSONObject();
        // TODO

        return json;

    }

    private JSONObject getMongoConfiguration(
            String host, int port,
            String database,
            String username, String password) {
        JSONObject json = new JSONObject();
        // TODO

        return json;

    }

}
