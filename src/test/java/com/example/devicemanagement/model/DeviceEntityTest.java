package com.example.devicemanagement.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.example.devicemanagement.enums.DeviceState;

class DeviceEntityTest {

    @Test
    void addDefaultsValues() {
        DeviceEntity entity = DeviceEntity.builder().name("device xyz").brand("Mac").build();

        entity.prePersist();

        Assertions.assertThat(entity.getId()).isNotNull();
        Assertions.assertThat(entity.getCreatedAt()).isNotNull();
        Assertions.assertThat(entity.getState()).isEqualTo(DeviceState.AVAILABLE);
    }

    @Test
    void keepsValuesAlreadySet() {
        UUID id = UUID.fromString("3f2a1c9e-1b2c-4d5e-8f90-1a2b3c4d5e6f");
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-09-19T16:07:48.163Z");

        DeviceEntity entity = DeviceEntity.builder()
                .id(id)
                .name("device xyz")
                .brand("Mac")
                .state(DeviceState.IN_USE)
                .createdAt(createdAt)
                .build();

        entity.prePersist();

        Assertions.assertThat(entity.getId()).isEqualTo(id);
        Assertions.assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        Assertions.assertThat(entity.getState()).isEqualTo(DeviceState.IN_USE);
    }
}
