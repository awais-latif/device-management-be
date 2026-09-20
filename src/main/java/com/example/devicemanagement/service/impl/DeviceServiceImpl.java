package com.example.devicemanagement.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.devicemanagement.exception.DeviceNotFoundException;
import com.example.devicemanagement.generated.model.CreateDeviceRequest;
import com.example.devicemanagement.generated.model.Device;
import com.example.devicemanagement.mapper.DeviceMapper;
import com.example.devicemanagement.repository.DeviceEntityRepository;
import com.example.devicemanagement.service.DeviceService;

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

    @Override
    @Transactional(readOnly = true)
    public Device getDevice(UUID id) {
        return deviceRepository.findById(id)
                .map(DeviceMapper::toDevice)
                .orElseThrow(() -> new DeviceNotFoundException(id));
    }
}
