package com.example.devicemanagement.converter;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.example.devicemanagement.exception.InvalidSortException;
import com.example.devicemanagement.generated.model.DeviceSortField;

@Component
public class StringToSortFieldConverter implements Converter<String, DeviceSortField> {

    @Override
    public DeviceSortField convert(String source) {
        return Arrays.stream(DeviceSortField.values())
                .filter(field -> field.getValue().equals(source))
                .findFirst()
                .orElseThrow(() -> new InvalidSortException(
                        "Unknown sort field: " + source + ". Allowed: " + allowedValues()));
    }

    private String allowedValues() {
        return Arrays.stream(DeviceSortField.values())
                .map(DeviceSortField::getValue)
                .collect(Collectors.joining(", "));
    }
}
