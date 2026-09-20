package com.example.devicemanagement.converter;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.example.devicemanagement.exception.InvalidSortException;
import com.example.devicemanagement.generated.model.SortDirection;

@Component
public class StringToSortDirectionConverter implements Converter<String, SortDirection> {

    @Override
    public SortDirection convert(String source) {
        return Arrays.stream(SortDirection.values())
                .filter(direction -> direction.getValue().equals(source))
                .findFirst()
                .orElseThrow(() -> new InvalidSortException(
                        "Unknown sort direction: " + source + ". Allowed: " + allowedValues()));
    }

    private String allowedValues() {
        return Arrays.stream(SortDirection.values())
                .map(SortDirection::getValue)
                .collect(Collectors.joining(", "));
    }
}
