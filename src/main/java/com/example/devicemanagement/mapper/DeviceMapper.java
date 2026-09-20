package com.example.devicemanagement.mapper;

import org.springframework.data.domain.Page;

import com.example.devicemanagement.enums.DeviceState;
import com.example.devicemanagement.generated.model.CreateDeviceRequest;
import com.example.devicemanagement.generated.model.Device;
import com.example.devicemanagement.generated.model.DevicePage;
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
     * Convert CreateDeviceRequest object to DeviceEntity object, if state is not provided, it will be availabel as default.
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
                .state(deviceRequest.getState() != null ? deviceRequest.getState() : DeviceState.AVAILABLE)
                .build();
    }

    /**
     * Convert Paged entity to DevicePage response.
     *
     * @param page page having entities
     * @return page response having content and page metadata
     */
    public static DevicePage toDevicePage(Page<DeviceEntity> page) {
        return new DevicePage()
                .content(page.getContent()
                        .stream()
                        .map(DeviceMapper::toDevice)
                        .toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages());
    }
}
