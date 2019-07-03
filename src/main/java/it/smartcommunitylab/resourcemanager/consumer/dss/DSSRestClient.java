package it.smartcommunitylab.resourcemanager.consumer.dss;

import java.nio.charset.Charset;
import java.util.Arrays;

import javax.servlet.http.HttpUtils;

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
        // DISABLED, dss rest bridge return pagination without any
        // info about total count or pages count
        throw new DSSException("unsupported on DSS rest");
//        try {
//            JSONArray datasets = null;
//
//            RestTemplate template = new RestTemplate();
//            HttpHeaders headers = headers(USERNAME, PASSWORD);
//
//            HttpEntity<String> entity = new HttpEntity<>(headers);
//
//            // fetch response as String because it may not match the json schema
//            ResponseEntity<String> response = template.exchange(ENDPOINT + API + TENANT + "/listDataService",
//                    HttpMethod.GET,
//                    entity, String.class);
//            if (response.getStatusCode() == HttpStatus.OK) {
//                // fetch JSON array
//                datasets = new JSONArray(response.getBody());
//            } else {
//                throw new DSSException("response error code " + response.getStatusCode());
//            }
//
//            return datasets;
//        } catch (RestClientException rex) {
//            throw new DSSException("rest error " + rex.getMessage());
//        }
    }

    public boolean hasSource(String name) throws DSSException {
        boolean exists = false;

        try {

            RestTemplate template = new RestTemplate();
            HttpHeaders headers = headers(USERNAME, PASSWORD);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // serviceid == name in DSS rest, mappend wrong
            String path = "/getDataService?serviceid=" + name;

            // fetch response as String because it may not match the json schema
            ResponseEntity<String> response = template.exchange(ENDPOINT + API + TENANT + path,
                    HttpMethod.GET,
                    entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                // assume OK means it exists, discard value..
                exists = true;
            } else {
                exists = false;
            }

        } catch (RestClientException rex) {
            // DSS will response with an error instead of 404..
            exists = false;
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
            JSONObject data = new JSONObject();

            data.put("name", name);
            data.put("description", type + ": " + name);

            JSONObject configs = new JSONObject();
            configs.put("id", name);
            configs.put("exposeAsODataService", true);
            configs.put("publicODataService", false);

            JSONArray properties = getProperties(type, host, port, database, username, password);
            if (properties == null) {
                throw new DSSException("error generating data source configuration");
            }

            configs.put("properties", properties);
            data.put("configs", configs);
            json.put("data", data);

            RestTemplate template = new RestTemplate();
            HttpHeaders headers = headers(USERNAME, PASSWORD);

            _log.trace("add source json " + json.toString());

            HttpEntity<String> entity = new HttpEntity<>(json.toString(), headers);

            // fetch response as String because it may not match the json schema
            ResponseEntity<String> response = template.exchange(ENDPOINT + API + TENANT + "/saveDataService",
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
        // TODO: verify if update is even possible given
        // the lack of access to data sources config in DSS rest
        return name;

    }

    public void deleteSource(String name) throws DSSException {
        try {
            RestTemplate template = new RestTemplate();
            HttpHeaders headers = headers(USERNAME, PASSWORD);

            JSONObject json = new JSONObject();
            HttpEntity<String> entity = new HttpEntity<>(json.toString(), headers);

            String path = "/dataService/" + name;

            // fetch response as String because it should be empty
            ResponseEntity<String> response = template.exchange(
                    ENDPOINT + API + TENANT + path, HttpMethod.DELETE, entity,
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

    private JSONArray getProperties(String type,
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

    private JSONArray getPostgresConfiguration(
            String host, int port,
            String database,
            String username, String password) {

        String connection = "jdbc:postgresql://" + host + ":" + Integer.toString(port) + "/" + database;

        JSONArray json = new JSONArray();

        JSONObject driver = new JSONObject();
        driver.put("name", "driverClassName");
        driver.put("value", "org.postgresql.Driver");
        json.put(driver);

        JSONObject url = new JSONObject();
        url.put("name", "url");
        url.put("value", connection);
        json.put(url);

        JSONObject user = new JSONObject();
        user.put("name", "username");
        user.put("value", username);
        json.put(user);

        JSONObject pass = new JSONObject();
        pass.put("name", "password");
        pass.put("value", password);
        json.put(pass);

        return json;

    }

    private JSONArray getMySqlConfiguration(
            String host, int port,
            String database,
            String username, String password) {
        String connection = "jdbc:mysql://" + host + ":" + Integer.toString(port) + "/" + database;

        JSONArray json = new JSONArray();

        JSONObject driver = new JSONObject();
        driver.put("name", "driverClassName");
        driver.put("value", "com.mysql.jdbc.Driver");
        json.put(driver);

        JSONObject url = new JSONObject();
        url.put("name", "url");
        url.put("value", connection);
        json.put(url);

        JSONObject user = new JSONObject();
        user.put("name", "username");
        user.put("value", username);
        json.put(user);

        JSONObject pass = new JSONObject();
        pass.put("name", "password");
        pass.put("value", password);
        json.put(pass);

        return json;

    }

    private JSONArray getMongoConfiguration(
            String host, int port,
            String database,
            String username, String password) {

        JSONArray json = new JSONArray();

        // not supported in DSS
        return json;

    }

}
