package com.example.devicemanagement.enums;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.example.devicemanagement.exception.UnknownDeviceStateException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DeviceState {
    AVAILABLE("available"),
    IN_USE("in-use"),
    INACTIVE("inactive");

    private final String value;

    DeviceState(String value) {
        this.value = value;
    }

    static final String allowedValue = Arrays.stream(values())
            .map(DeviceState::getValue)
            .collect(Collectors.joining(", "));

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static DeviceState forValue(String value) {
        for (DeviceState e : DeviceState.values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        throw new UnknownDeviceStateException("Unknown state value: " + value + ". Allowed: " + allowedValue);
    }
}
