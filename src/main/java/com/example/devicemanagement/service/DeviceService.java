package com.example.devicemanagement.service;

import java.util.UUID;

import com.example.devicemanagement.generated.model.CreateDeviceRequest;
import com.example.devicemanagement.generated.model.Device;

public interface DeviceService {

    /**
     * Create device, if state is not provided, default state will be available.
     *
     * @param request request object
     * @return response object
     */
    Device createDevice(CreateDeviceRequest request);

    /**
     * Get a single device having given id.
     *
     * @param id id of the device
     * @return the device
     * @throws com.example.devicemanagement.exception.DeviceNotFoundException if no device has that id
     */
    Device getDevice(UUID id);

    /**
     * Delete a device. If device is in-use state, it can not be deleted.
     *
     * @param id id of the device
     * @throws com.example.devicemanagement.exception.DeviceNotFoundException if no device has that id
     * @throws com.example.devicemanagement.exception.DeviceInUseException    if the device is in use
     */
    void deleteDevice(UUID id);
}
