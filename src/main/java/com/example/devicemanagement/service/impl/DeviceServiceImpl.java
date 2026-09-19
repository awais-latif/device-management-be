package com.example.devicemanagement.service.impl;

import org.springframework.stereotype.Service;

import com.example.devicemanagement.generated.model.CreateDeviceRequest;
import com.example.devicemanagement.generated.model.Device;
import com.example.devicemanagement.mapper.DeviceMapper;
import com.example.devicemanagement.repository.DeviceEntityRepository;
import com.example.devicemanagement.service.DeviceService;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceEntityRepository deviceRepository;

    @Override
    @Transactional
    public Device createDevice(CreateDeviceRequest request) {
        var device = DeviceMapper.toDeviceEntity(request);
        var savedDevice = deviceRepository.save(device);
        return DeviceMapper.toDevice(savedDevice);
    }
}
