package com.example.devicemanagement.controller;

import java.util.UUID;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.example.devicemanagement.enums.DeviceState;
import com.example.devicemanagement.generated.api.DevicesApi;
import com.example.devicemanagement.generated.model.CreateDeviceRequest;
import com.example.devicemanagement.generated.model.Device;
import com.example.devicemanagement.generated.model.DevicePage;
import com.example.devicemanagement.generated.model.PatchDeviceRequest;
import com.example.devicemanagement.service.DeviceService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class DeviceController implements DevicesApi {
    private final DeviceService deviceService;

    @Override
    public ResponseEntity<Device> createDevice(CreateDeviceRequest createDeviceRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deviceService.createDevice(createDeviceRequest));
    }

    @Override
    public ResponseEntity<Void> deleteDevice(UUID id) {
        //TODO implement
        throw new NotImplementedException("Not Implemented");
    }

    @Override
    public ResponseEntity<Device> getDevice(UUID id) {
        return ResponseEntity.ok(deviceService.getDevice(id));
    }

    @Override
    public ResponseEntity<DevicePage> listDevices(String brand, DeviceState state, Integer page, Integer size,
            String sortBy, String sortDirection) {
        //TODO implement
        throw new NotImplementedException("Not Implemented");
    }

    @Override
    public ResponseEntity<Device> updateDevice(UUID id, PatchDeviceRequest patchDeviceRequest) {
        //TODO implement
        throw new NotImplementedException("Not Implemented");
    }
}
