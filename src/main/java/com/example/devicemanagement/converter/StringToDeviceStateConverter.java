package com.example.devicemanagement.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.example.devicemanagement.enums.DeviceState;

@Component
public class StringToDeviceStateConverter implements Converter<String, DeviceState> {

    @Override
    public DeviceState convert(String source) {
        return DeviceState.forValue(source);
    }
}
