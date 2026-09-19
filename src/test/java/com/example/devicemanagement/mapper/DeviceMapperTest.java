package com.example.devicemanagement.mapper;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.example.devicemanagement.enums.DeviceState;
import com.example.devicemanagement.generated.model.CreateDeviceRequest;
import com.example.devicemanagement.generated.model.Device;
import com.example.devicemanagement.model.DeviceEntity;

class DeviceMapperTest {

    private static final UUID ID = UUID.fromString("3f2a1c9e-1b2c-4d5e-8f90-1a2b3c4d5e6f");
    private static final OffsetDateTime CREATED_AT = OffsetDateTime.parse("2026-09-19T16:07:48.163Z");
    private static final OffsetDateTime UPDATED_AT = OffsetDateTime.parse("2026-09-20T09:00:00.000Z");

    @Test
    void mapsEntityToResponse() {
        DeviceEntity entity = DeviceEntity.builder()
                .id(ID)
                .name("device xyz")
                .brand("Mac")
                .state(DeviceState.IN_USE)
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .version(3L)
                .build();

        Device device = DeviceMapper.toDevice(entity);

        Assertions.assertThat(device.getId())
                .isEqualTo(ID);
        Assertions.assertThat(device.getName())
                .isEqualTo("device xyz");
        Assertions.assertThat(device.getBrand())
                .isEqualTo("Mac");
        Assertions.assertThat(device.getState())
                .isEqualTo(DeviceState.IN_USE);
        Assertions.assertThat(device.getCreatedAt())
                .isEqualTo(CREATED_AT);
        Assertions.assertThat(device.getUpdatedAt())
                .isEqualTo(UPDATED_AT);
    }

    @Test
    void mapsRequestToEntity() {
        CreateDeviceRequest request = new CreateDeviceRequest()
                .name("device xyz")
                .brand("Mac")
                .state(DeviceState.INACTIVE);

        DeviceEntity entity = DeviceMapper.toDeviceEntity(request);

        Assertions.assertThat(entity.getName())
                .isEqualTo("device xyz");
        Assertions.assertThat(entity.getBrand())
                .isEqualTo("Mac");
        Assertions.assertThat(entity.getState())
                .isEqualTo(DeviceState.INACTIVE);
        Assertions.assertThat(entity.getId())
                .isNull();
        Assertions.assertThat(entity.getCreatedAt())
                .isNull();
        Assertions.assertThat(entity.getVersion())
                .isNull();
    }

    @Test
    void checkNull() {
        Assertions.assertThat(DeviceMapper.toDevice(null))
                .isNull();
        Assertions.assertThat(DeviceMapper.toDeviceEntity(null))
                .isNull();
    }
}
