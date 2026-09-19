package com.example.devicemanagement.service;

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
}
