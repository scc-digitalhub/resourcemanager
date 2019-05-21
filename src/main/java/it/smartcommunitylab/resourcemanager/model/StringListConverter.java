package it.smartcommunitylab.resourcemanager.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.AttributeConverter;

public class StringListConverter implements AttributeConverter<List<String>, String> {
    private static final String SPLIT_CHAR = ";";

    @Override
    public String convertToDatabaseColumn(List<String> list) {
        if (list != null) {
            return String.join(SPLIT_CHAR, list);
        } else {
            return "";
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String string) {
        if (string != null) {
            return Arrays.asList(string.split(SPLIT_CHAR));
        } else {
            return new ArrayList<>();
        }
    }
}