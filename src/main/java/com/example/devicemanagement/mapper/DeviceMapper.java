package com.example.devicemanagement.mapper;

import com.example.devicemanagement.generated.model.CreateDeviceRequest;
import com.example.devicemanagement.generated.model.Device;
import com.example.devicemanagement.model.DeviceEntity;

public class DeviceMapper {
    private DeviceMapper() {
    }

    /**
     * Convert DeviceEntity object to Device response object.
     *
     * @param deviceEntity entity object
     * @return null if entity object is null, otherwise return Device response object
     */
    public static Device toDevice(DeviceEntity deviceEntity) {
        if (deviceEntity == null) {
            return null;
        }
        return new Device().id(deviceEntity.getId())
                .name(deviceEntity.getName())
                .brand(deviceEntity.getBrand())
                .createdAt(deviceEntity.getCreatedAt())
                .state(deviceEntity.getState())
                .updatedAt(deviceEntity.getUpdatedAt());
    }

    /**
     * Convert CreateDeviceRequest object to DeviceEntity object.
     *
     * @param deviceRequest request object
     * @return null if request object is null, otherwise return DeviceEntity object
     */
    public static DeviceEntity toDeviceEntity(CreateDeviceRequest deviceRequest) {
        if (deviceRequest == null) {
            return null;
        }
        return DeviceEntity.builder()
                .name(deviceRequest.getName())
                .brand(deviceRequest.getBrand())
                .state(deviceRequest.getState())
                .build();
    }
}
