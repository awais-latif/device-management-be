package com.example.devicemanagement.model;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import com.example.devicemanagement.enums.DeviceState;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "device")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DeviceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Setter
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Setter
    @Column(name = "brand", nullable = false, length = 255)
    private String brand;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    private DeviceState state;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
    
    @PrePersist
    void prePersist() {
        createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
}
