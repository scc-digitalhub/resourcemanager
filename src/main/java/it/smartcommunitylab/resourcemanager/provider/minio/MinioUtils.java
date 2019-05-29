package it.smartcommunitylab.resourcemanager.provider.minio;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

public class MinioUtils {

    public static boolean isValid(String uri) {
        // simple check for provider match
        // TODO validate format
        return uri.startsWith("minio://");
    }

    public static String encodeURI(
            String endpoint, String bucket,
            String accessKey, String secretKey) {

        // pack connection details into URL
        // <provider>://<host>:<port>/<bucket>?accessKey=<accessKey>&secretKey=<secretKey>
        StringBuilder sb = new StringBuilder();
        sb.append("minio://");
        try {
            // endpoint should be host:port from caller
            sb.append(endpoint);
            sb.append("/");
            sb.append(URLEncoder.encode(bucket, "UTF-8"));
            sb.append("?accessKey=").append(URLEncoder.encode(accessKey, "UTF-8"));
            sb.append("&secretKey=").append(URLEncoder.encode(secretKey, "UTF-8"));

        } catch (UnsupportedEncodingException e) {
        }

        return sb.toString();
    }

    public static String getAccessKey(String uri) {
        if (!isValid(uri)) {
            return "";
        }

        try {
            // replace minio with http for URL parsing
            String url = uri.replace("minio://", "http://");
            MultiValueMap<String, String> parameters = UriComponentsBuilder.fromUriString(url).build().getQueryParams();
            List<String> values = parameters.get("accessKey");
            if (values != null && !values.isEmpty()) {
                return values.get(0);
            } else {
                return "";
            }

        } catch (Exception e) {
            return "";
        }
    }

    public static String getSecretKey(String uri) {
        if (!isValid(uri)) {
            return "";
        }

        try {
            // replace minio with http for URL parsing
            String url = uri.replace("minio://", "http://");
            MultiValueMap<String, String> parameters = UriComponentsBuilder.fromUriString(url).build().getQueryParams();
            List<String> values = parameters.get("secretKey");
            if (values != null && !values.isEmpty()) {
                return values.get(0);
            } else {
                return "";
            }

        } catch (Exception e) {
            return "";
        }
    }

    public static String getEndpoint(String uri) {
        if (!isValid(uri)) {
            return "";
        }

        try {
            URI u = new URI(uri);
            return u.getAuthority();
        } catch (Exception e) {
            return "";
        }
    }

    public static String getHost(String uri) {
        if (!isValid(uri)) {
            return "";
        }

        try {
            URI u = new URI(uri);
            return u.getHost();
        } catch (Exception e) {
            return "";
        }
    }

    public static int getPort(String uri) {
        if (!isValid(uri)) {
            return -1;
        }

        try {
            URI u = new URI(uri);
            return u.getPort();
        } catch (Exception e) {
            return -1;
        }
    }

    public static String getBucket(String uri) {
        if (!isValid(uri)) {
            return "";
        }

        try {
            URI u = new URI(uri);
            String path = u.getPath();
            if (path.startsWith("/")) {
                return path.substring(1);
            } else {
                return path;
            }
        } catch (Exception e) {
            return "";
        }
    }

}
