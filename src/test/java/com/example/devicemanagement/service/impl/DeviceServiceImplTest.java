package com.example.devicemanagement.service.impl;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.devicemanagement.enums.DeviceState;
import com.example.devicemanagement.generated.model.CreateDeviceRequest;
import com.example.devicemanagement.generated.model.Device;
import com.example.devicemanagement.model.DeviceEntity;
import com.example.devicemanagement.repository.DeviceEntityRepository;

@ExtendWith(MockitoExtension.class)
class DeviceServiceImplTest {

    private static final UUID ID = UUID.fromString("3f2a1c9e-1b2c-4d5e-8f90-1a2b3c4d5e6f");
    private static final OffsetDateTime CREATED_AT = OffsetDateTime.parse("2026-09-19T16:07:48.163Z");

    @Mock
    private DeviceEntityRepository deviceRepository;

    @InjectMocks
    private DeviceServiceImpl deviceService;

    @Captor
    private ArgumentCaptor<DeviceEntity> entityCaptor;

    @Test
    void createAndReturnsDevice() {
        CreateDeviceRequest request = new CreateDeviceRequest()
                .name("device xyz")
                .brand("Mac")
                .state(DeviceState.IN_USE);

        Mockito.when(deviceRepository.save(ArgumentMatchers.any(DeviceEntity.class)))
                .thenReturn(DeviceEntity.builder()
                        .id(ID)
                        .name("device xyz")
                        .brand("Mac")
                        .state(DeviceState.IN_USE)
                        .createdAt(CREATED_AT)
                        .version(0L)
                        .build());

        Device created = deviceService.createDevice(request);

        Mockito.verify(deviceRepository).save(entityCaptor.capture());
        DeviceEntity persisted = entityCaptor.getValue();
        Assertions.assertThat(persisted.getName()).isEqualTo("device xyz");
        Assertions.assertThat(persisted.getBrand()).isEqualTo("Mac");
        Assertions.assertThat(persisted.getState()).isEqualTo(DeviceState.IN_USE);

        Assertions.assertThat(created.getId()).isEqualTo(ID);
        Assertions.assertThat(created.getCreatedAt()).isEqualTo(CREATED_AT);
        Assertions.assertThat(created.getState()).isEqualTo(DeviceState.IN_USE);
    }
}
