package it.smartcommunitylab.resourcemanager.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class OdbcUtil {

    public static String encodeURI(
            String provider, String endpoint, String database,
            String username, String password) {

        // pack connection details into URL
        // mimic RFC 1738
        // <provider>://<user>:<password>@<host>:<port>/<resource>
        StringBuilder sb = new StringBuilder();
        sb.append(provider).append("://");
        try {
            sb.append(URLEncoder.encode(username, "UTF-8"));
            sb.append(":");
            sb.append(URLEncoder.encode(password, "UTF-8"));
            sb.append("@");
            // endpoint should be host:port from caller
            sb.append(endpoint);
            sb.append("/");
            sb.append(URLEncoder.encode(database, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
        }

        return sb.toString();
    }

    public static String encodeURI(
            String provider, String endpoint, String database) {

        // pack connection details into URL
        // mimic RFC 1738
        // <provider>://<host>:<port>/<resource>
        StringBuilder sb = new StringBuilder();
        sb.append(provider).append("://");
        try {
            // endpoint should be host:port from caller
            sb.append(endpoint);
            sb.append("/");
            sb.append(URLEncoder.encode(database, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
        }

        return sb.toString();
    }

    public static String getUsername(String uri) {
        try {
            URI u = new URI(uri);
            String userInfo = u.getUserInfo();
            String userName = "";
            if (userInfo == null) {
                return userName;
            }

            if (userInfo.contains(":")) {
                userName = userInfo.split(":")[0];
            } else {
                userName = userInfo;
            }

            return URLDecoder.decode(userName, "UTF-8");
        } catch (Exception e) {
            return "";
        }
    }

    public static String getPassword(String uri) {
        try {
            URI u = new URI(uri);
            String userInfo = u.getUserInfo();
            String password = "";
            if (userInfo == null) {
                return password;
            }

            if (userInfo.contains(":")) {
                password = userInfo.split(":")[1];
            } else {
                password = "";
            }

            return URLDecoder.decode(password, "UTF-8");
        } catch (Exception e) {
            return "";
        }
    }

    public static String getProvider(String uri) {
        try {
            URI u = new URI(uri);
            return u.getScheme();
        } catch (Exception e) {
            return "";
        }
    }

    public static String getEndpoint(String uri) {
        try {
            URI u = new URI(uri);
            return u.getAuthority();
        } catch (Exception e) {
            return "";
        }
    }

    public static String getHost(String uri) {
        try {
            URI u = new URI(uri);
            return u.getHost();
        } catch (Exception e) {
            return "";
        }
    }

    public static int getPort(String uri) {
        try {
            URI u = new URI(uri);
            return u.getPort();
        } catch (Exception e) {
            return -1;
        }
    }

    public static String getDatabase(String uri) {
        try {
            URI u = new URI(uri);
            String path = u.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            return URLDecoder.decode(path, "UTF-8");
        } catch (Exception e) {
            return "";
        }
    }
}
