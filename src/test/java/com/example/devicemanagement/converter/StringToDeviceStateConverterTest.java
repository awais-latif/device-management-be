package com.example.devicemanagement.converter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.example.devicemanagement.enums.DeviceState;
import com.example.devicemanagement.exception.UnknownDeviceStateException;

class StringToDeviceStateConverterTest {

    private final StringToDeviceStateConverter converter = new StringToDeviceStateConverter();

    @Test
    void convertsValue() {
        Assertions.assertThat(converter.convert("in-use")).isEqualTo(DeviceState.IN_USE);
    }

    @Test
    void passesUnknownValue() {
        Assertions.assertThatThrownBy(() -> converter.convert("unknown"))
                .isInstanceOf(UnknownDeviceStateException.class);
    }
}
