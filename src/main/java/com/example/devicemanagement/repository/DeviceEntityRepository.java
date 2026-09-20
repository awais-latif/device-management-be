package com.example.devicemanagement.repository;

import java.util.UUID;

import com.example.devicemanagement.model.DeviceEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DeviceEntityRepository
        extends JpaRepository<DeviceEntity, UUID>, JpaSpecificationExecutor<DeviceEntity> {
}
