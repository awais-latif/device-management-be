package com.example.devicemanagement.enums;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.example.devicemanagement.exception.UnknownDeviceStateException;

class DeviceStateTest {

    @Test
    void checkValues() {
        Assertions.assertThat(DeviceState.forValue("available")).isEqualTo(DeviceState.AVAILABLE);
        Assertions.assertThat(DeviceState.forValue("in-use")).isEqualTo(DeviceState.IN_USE);
        Assertions.assertThat(DeviceState.forValue("inactive")).isEqualTo(DeviceState.INACTIVE);

        Assertions.assertThat(DeviceState.AVAILABLE.getValue()).isEqualTo("available");
        Assertions.assertThat(DeviceState.IN_USE.getValue()).isEqualTo("in-use");
        Assertions.assertThat(DeviceState.INACTIVE.getValue()).isEqualTo("inactive");
    }

    @Test
    void rejectsUnknownValue() {
        Assertions.assertThatThrownBy(() -> DeviceState.forValue("Unknown"))
                .isInstanceOf(UnknownDeviceStateException.class)
                .hasMessage("Unknown state value: Unknown. Allowed: available, in-use, inactive");
    }

    @Test
    void isCaseSensitive() {
        Assertions.assertThatThrownBy(() -> DeviceState.forValue("AVAILABLE"))
                .isInstanceOf(UnknownDeviceStateException.class);
    }
}
