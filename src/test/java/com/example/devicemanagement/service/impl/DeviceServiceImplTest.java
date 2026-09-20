package com.example.devicemanagement.service.impl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.example.devicemanagement.enums.DeviceState;
import com.example.devicemanagement.exception.DeviceInUseException;
import com.example.devicemanagement.exception.DeviceNotFoundException;
import com.example.devicemanagement.generated.model.CreateDeviceRequest;
import com.example.devicemanagement.generated.model.Device;
import com.example.devicemanagement.generated.model.DevicePage;
import com.example.devicemanagement.generated.model.DeviceSortField;
import com.example.devicemanagement.generated.model.PatchDeviceRequest;
import com.example.devicemanagement.generated.model.SortDirection;
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

    @Test
    void returnsDeviceById() {
        Mockito.when(deviceRepository.findById(ID))
                .thenReturn(Optional.of(DeviceEntity.builder()
                        .id(ID)
                        .name("device xyz")
                        .brand("Mac")
                        .state(DeviceState.AVAILABLE)
                        .createdAt(CREATED_AT)
                        .version(0L)
                        .build()));

        Device device = deviceService.getDevice(ID);

        Assertions.assertThat(device.getId())
                .isEqualTo(ID);
        Assertions.assertThat(device.getName())
                .isEqualTo("device xyz");
        Assertions.assertThat(device.getState())
                .isEqualTo(DeviceState.AVAILABLE);
    }

    @Test
    void deletesDeviceThatIsNotInUse() {
        DeviceEntity stored = DeviceEntity.builder()
                .id(ID)
                .name("device xyz")
                .brand("Mac")
                .state(DeviceState.AVAILABLE)
                .createdAt(CREATED_AT)
                .version(0L)
                .build();
        Mockito.when(deviceRepository.findById(ID))
                .thenReturn(Optional.of(stored));

        deviceService.deleteDevice(ID);

        Mockito.verify(deviceRepository)
                .delete(stored);
    }

    @Test
    void notAllowToDeleteDeviceInUse() {
        DeviceEntity stored = DeviceEntity.builder()
                .id(ID)
                .name("device xyz")
                .brand("Mac")
                .state(DeviceState.IN_USE)
                .createdAt(CREATED_AT)
                .version(0L)
                .build();
        Mockito.when(deviceRepository.findById(ID))
                .thenReturn(Optional.of(stored));

        Assertions.assertThatThrownBy(() -> deviceService.deleteDevice(ID))
                .isInstanceOf(DeviceInUseException.class)
                .hasMessage("Device is in use and cannot be deleted");

        Mockito.verify(deviceRepository, Mockito.never())
                .delete(ArgumentMatchers.any(DeviceEntity.class));
    }

    @Test
    void failsToDeleteMissingDevice() {
        Mockito.when(deviceRepository.findById(ID))
                .thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> deviceService.deleteDevice(ID))
                .isInstanceOf(DeviceNotFoundException.class);

        Mockito.verify(deviceRepository, Mockito.never())
                .delete(ArgumentMatchers.any(DeviceEntity.class));
    }

    @Test
    void failsWhenDeviceMissing() {
        Mockito.when(deviceRepository.findById(ID))
                .thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> deviceService.getDevice(ID))
                .isInstanceOf(DeviceNotFoundException.class)
                .hasMessage("Device not found for Id: " + ID);
    }

    @Test
    void getDevicesUsingTheRequestedPage() {
        DeviceEntity stored = DeviceEntity.builder()
                .id(ID)
                .name("device xyz")
                .brand("Mac")
                .state(DeviceState.AVAILABLE)
                .createdAt(CREATED_AT)
                .version(0L)
                .build();
        Mockito.when(deviceRepository.findAll(ArgumentMatchers.<Specification<DeviceEntity>>any(),
                        ArgumentMatchers.any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(List.of(stored), invocation.getArgument(1), 11));

        DevicePage page = deviceService.getDevices("Mac", DeviceState.AVAILABLE, 2, 5, DeviceSortField.NAME, SortDirection.ASC);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        Mockito.verify(deviceRepository)
                .findAll(ArgumentMatchers.<Specification<DeviceEntity>>any(), pageable.capture());

        Assertions.assertThat(pageable.getValue()
                        .getPageNumber())
                .isEqualTo(2);
        Assertions.assertThat(pageable.getValue()
                        .getPageSize())
                .isEqualTo(5);
        Assertions.assertThat(pageable.getValue()
                        .getSort()
                        .toString())
                .isEqualTo("name: ASC,id: ASC");

        Assertions.assertThat(page.getContent())
                .singleElement()
                .extracting(Device::getId)
                .isEqualTo(ID);
        Assertions.assertThat(page.getTotalElements())
                .isEqualTo(11L);
    }

    @Test
    void sortsByCreatedAtDescendingByDefault() {
        Mockito.when(deviceRepository.findAll(ArgumentMatchers.<Specification<DeviceEntity>>any(),
                        ArgumentMatchers.any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(List.of(), invocation.getArgument(1), 0));

        deviceService.getDevices(null, null, 0, 20, DeviceSortField.CREATED_AT, SortDirection.DESC);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        Mockito.verify(deviceRepository)
                .findAll(ArgumentMatchers.<Specification<DeviceEntity>>any(), pageable.capture());

        Assertions.assertThat(pageable.getValue()
                        .getSort()
                        .toString())
                .isEqualTo("createdAt: DESC,id: ASC");
    }

    @Test
    void changeOnlyFieldsProvidedInRequest() {
        DeviceEntity stored = storedDevice(DeviceState.AVAILABLE);
        Mockito.when(deviceRepository.findById(ID))
                .thenReturn(Optional.of(stored));
        Mockito.when(deviceRepository.saveAndFlush(stored))
                .thenReturn(stored);

        Device updated = deviceService.updateDevice(ID, new PatchDeviceRequest().name("device abc"));

        Assertions.assertThat(updated.getName())
                .isEqualTo("device abc");
        Assertions.assertThat(stored.getBrand())
                .isEqualTo("Mac");
        Assertions.assertThat(stored.getState())
                .isEqualTo(DeviceState.AVAILABLE);
    }

    @Test
    void notAllowToChangenameDeviceInUse() {
        DeviceEntity stored = storedDevice(DeviceState.IN_USE);
        Mockito.when(deviceRepository.findById(ID))
                .thenReturn(Optional.of(stored));

        Assertions.assertThatThrownBy(
                        () -> deviceService.updateDevice(ID, new PatchDeviceRequest().name("device abc")))
                .isInstanceOf(DeviceInUseException.class)
                .hasMessage("Device is in use, name and brand cannot be changed");

        Assertions.assertThat(stored.getName())
                .isEqualTo("device xyz");
        Mockito.verify(deviceRepository, Mockito.never())
                .saveAndFlush(ArgumentMatchers.any(DeviceEntity.class));
    }

    @Test
    void notAllowToChangeBrandDeviceInUse() {
        DeviceEntity stored = storedDevice(DeviceState.IN_USE);
        Mockito.when(deviceRepository.findById(ID))
                .thenReturn(Optional.of(stored));

        Assertions.assertThatThrownBy(
                        () -> deviceService.updateDevice(ID, new PatchDeviceRequest().brand("Dell")))
                .isInstanceOf(DeviceInUseException.class)
                .hasMessage("Device is in use, name and brand cannot be changed");
    }

    @Test
    void allowsStateChangeWhileInUse() {
        DeviceEntity stored = storedDevice(DeviceState.IN_USE);
        Mockito.when(deviceRepository.findById(ID))
                .thenReturn(Optional.of(stored));
        Mockito.when(deviceRepository.saveAndFlush(stored))
                .thenReturn(stored);

        Device updated = deviceService.updateDevice(ID,
                new PatchDeviceRequest().state(DeviceState.AVAILABLE));

        Assertions.assertThat(updated.getState())
                .isEqualTo(DeviceState.AVAILABLE);
    }

    @Test
    void allowsSameNameAndBrandWhileInUse() {
        DeviceEntity stored = storedDevice(DeviceState.IN_USE);
        Mockito.when(deviceRepository.findById(ID))
                .thenReturn(Optional.of(stored));
        Mockito.when(deviceRepository.saveAndFlush(stored))
                .thenReturn(stored);

        Device updated = deviceService.updateDevice(ID, new PatchDeviceRequest()
                .name("device xyz")
                .brand("Mac")
                .state(DeviceState.AVAILABLE));

        Assertions.assertThat(updated.getState())
                .isEqualTo(DeviceState.AVAILABLE);
    }

    @Test
    void checkIfNameChangeChangeStateInUseToAvailable() {
        DeviceEntity stored = storedDevice(DeviceState.IN_USE);
        Mockito.when(deviceRepository.findById(ID))
                .thenReturn(Optional.of(stored));

        Assertions.assertThatThrownBy(() -> deviceService.updateDevice(ID, new PatchDeviceRequest()
                        .name("renamed")
                        .state(DeviceState.AVAILABLE)))
                .isInstanceOf(DeviceInUseException.class);
    }

    @Test
    void trimsNameAndBrandOnUpdate() {
        DeviceEntity stored = storedDevice(DeviceState.AVAILABLE);
        Mockito.when(deviceRepository.findById(ID))
                .thenReturn(Optional.of(stored));
        Mockito.when(deviceRepository.saveAndFlush(stored))
                .thenReturn(stored);

        deviceService.updateDevice(ID, new PatchDeviceRequest()
                .name("  device abc  ")
                .brand("  Dell  "));

        Assertions.assertThat(stored.getName())
                .isEqualTo("device abc");
        Assertions.assertThat(stored.getBrand())
                .isEqualTo("Dell");
    }

    @Test
    void failsToUpdateNotExitingDevice() {
        Mockito.when(deviceRepository.findById(ID))
                .thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(
                        () -> deviceService.updateDevice(ID, new PatchDeviceRequest().name("renamed")))
                .isInstanceOf(DeviceNotFoundException.class);
    }

    private DeviceEntity storedDevice(DeviceState state) {
        return DeviceEntity.builder()
                .id(ID)
                .name("device xyz")
                .brand("Mac")
                .state(state)
                .createdAt(CREATED_AT)
                .version(0L)
                .build();
    }
}
