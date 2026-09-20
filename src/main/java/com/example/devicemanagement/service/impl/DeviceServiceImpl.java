package com.example.devicemanagement.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.devicemanagement.enums.DeviceState;
import com.example.devicemanagement.exception.DeviceInUseException;
import com.example.devicemanagement.exception.DeviceNotFoundException;
import com.example.devicemanagement.generated.model.CreateDeviceRequest;
import com.example.devicemanagement.generated.model.Device;
import com.example.devicemanagement.generated.model.DevicePage;
import com.example.devicemanagement.generated.model.DeviceSortField;
import com.example.devicemanagement.generated.model.PatchDeviceRequest;
import com.example.devicemanagement.generated.model.SortDirection;
import com.example.devicemanagement.mapper.DeviceMapper;
import com.example.devicemanagement.model.DeviceEntity;
import com.example.devicemanagement.repository.DeviceEntityRepository;
import com.example.devicemanagement.service.DeviceService;

import jakarta.persistence.criteria.Predicate;
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

    @Override
    @Transactional
    public void deleteDevice(UUID id) {
        DeviceEntity device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));

        if (DeviceState.IN_USE == device.getState()) {
            throw new DeviceInUseException("Device is in use and cannot be deleted");
        }

        deviceRepository.delete(device);
    }

    @Override
    @Transactional
    public Device updateDevice(UUID id, PatchDeviceRequest request) {
        DeviceEntity device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));

        if (DeviceState.IN_USE == device.getState()) {
            validatedNameAndBrandChanged(device, request);
        }

        if (request.getName() != null) {
            device.setName(request.getName());
        }
        if (request.getBrand() != null) {
            device.setBrand(request.getBrand());
        }
        if (request.getState() != null) {
            device.setState(request.getState());
        }

        return DeviceMapper.toDevice(deviceRepository.saveAndFlush(device));
    }

    private void validatedNameAndBrandChanged(DeviceEntity device, PatchDeviceRequest request) {
        boolean nameChanged = request.getName() != null && !request.getName()
                .equals(device.getName());
        boolean branchChanged = request.getBrand() != null && !request.getBrand()
                .equals(device.getBrand());

        if (nameChanged || branchChanged) {
            throw new DeviceInUseException("Device is in use, name and brand cannot be changed");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DevicePage getDevices(String brand, DeviceState state, Integer page, Integer size,
            DeviceSortField sortBy, SortDirection sortDirection) {

        return DeviceMapper.toDevicePage(deviceRepository.findAll(getSpecification(brand, state),
                toPageable(page, size, sortBy, sortDirection)));
    }

    private Specification<DeviceEntity> getSpecification(String brand, DeviceState state) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (brand != null) {
                predicates.add(builder.equal(root.get("brand"), brand));
            }
            if (state != null) {
                predicates.add(builder.equal(root.get("state"), state));
            }

            return predicates.isEmpty() ? null : builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Create object of Pageable from given data, id field is used for tiebreaking when given sort field is same.
     *
     * @param page          page number
     * @param size          total items per page
     * @param sortBy        sort by field
     * @param sortDirection sort direction
     * @return Pageable object, can be used in repository
     */
    private Pageable toPageable(Integer page, Integer size, DeviceSortField sortBy, SortDirection sortDirection) {
        Sort.Direction direction = SortDirection.ASC == sortDirection ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(direction, sortBy.getValue())
                .and(Sort.by("id")));
    }
}
