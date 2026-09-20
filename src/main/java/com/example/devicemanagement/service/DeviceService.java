package com.example.devicemanagement.service;

import java.util.UUID;

import com.example.devicemanagement.enums.DeviceState;
import com.example.devicemanagement.generated.model.CreateDeviceRequest;
import com.example.devicemanagement.generated.model.Device;
import com.example.devicemanagement.generated.model.DevicePage;
import com.example.devicemanagement.generated.model.DeviceSortField;
import com.example.devicemanagement.generated.model.PatchDeviceRequest;
import com.example.devicemanagement.generated.model.SortDirection;

public interface DeviceService {

    /**
     * Create device, if state is not provided, default state will be available.
     *
     * @param request request object
     * @return response object
     */
    Device createDevice(CreateDeviceRequest request);

    /**
     * Get a single device having given id.
     *
     * @param id id of the device
     * @return the device
     * @throws com.example.devicemanagement.exception.DeviceNotFoundException if no device has that id
     */
    Device getDevice(UUID id);

    /**
     * Delete a device. If device is in-use state, it can not be deleted.
     *
     * @param id id of the device
     * @throws com.example.devicemanagement.exception.DeviceNotFoundException if no device has that id
     * @throws com.example.devicemanagement.exception.DeviceInUseException    if the device is in use
     */
    void deleteDevice(UUID id);

    /**
     * Update a device. Only the fields present in the request are applied. Name and brand can not be changed if device is in-use, state can always be
     * changed.
     *
     * @param id      id of the device
     * @param request request to change
     * @return the updated device
     * @throws com.example.devicemanagement.exception.DeviceNotFoundException if no device has that id
     * @throws com.example.devicemanagement.exception.DeviceInUseException    if the device is in use and name or brand changes
     */
    Device updateDevice(UUID id, PatchDeviceRequest request);

    /**
     * Get a page of devices, optionally filter by brand and/or state.
     *
     * @param brand         brand for filter, null means no filter needed
     * @param state         state for filter, null means no filter needed
     * @param page          zero based page index
     * @param size          max items per page
     * @param sortBy        field to sort on
     * @param sortDirection asc or desc
     * @return the matching page with content and page metadata
     */
    DevicePage getDevices(String brand, DeviceState state, Integer page, Integer size, DeviceSortField sortBy,
            SortDirection sortDirection);
}
