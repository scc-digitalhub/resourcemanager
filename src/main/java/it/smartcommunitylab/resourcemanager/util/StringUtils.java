package it.smartcommunitylab.resourcemanager.util;

public class StringUtils {

    public static String shorten(String value, int length) {
        if (value.length() <= length) {
            return value;
        }

        return value.substring(0, length);

    }
}
