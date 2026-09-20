package com.example.devicemanagement.controller;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.example.devicemanagement.enums.DeviceState;
import com.example.devicemanagement.exception.DeviceInUseException;
import com.example.devicemanagement.exception.DeviceNotFoundException;
import com.example.devicemanagement.generated.model.CreateDeviceRequest;
import com.example.devicemanagement.generated.model.Device;
import com.example.devicemanagement.generated.model.DevicePage;
import com.example.devicemanagement.generated.model.DeviceSortField;
import com.example.devicemanagement.generated.model.PatchDeviceRequest;
import com.example.devicemanagement.generated.model.SortDirection;
import com.example.devicemanagement.service.DeviceService;

@WebMvcTest(DeviceController.class)
class DeviceControllerTest {

    private static final String DEVICES = "/api/v1/devices";
    private static final UUID ID = UUID.fromString("3f2a1c9e-1b2c-4d5e-8f90-1a2b3c4d5e6f");
    private static final String VALID_PAYLOAD = """
            {"name":"device xyz","brand":"Mac","state":"in-use"}""";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeviceService deviceService;

    @Test
    void createsDevice() throws Exception {
        Mockito.when(deviceService.createDevice(ArgumentMatchers.any(CreateDeviceRequest.class)))
                .thenReturn(new Device()
                        .id(ID)
                        .name("device xyz")
                        .brand("Mac")
                        .state(DeviceState.IN_USE)
                        .createdAt(OffsetDateTime.parse("2026-09-19T16:07:48.163Z")));

        mockMvc.perform(MockMvcRequestBuilders.post(DEVICES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PAYLOAD))
                .andExpect(MockMvcResultMatchers.status()
                        .isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id")
                        .value(ID.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.state")
                        .value("in-use"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt")
                        .value("2026-09-19T16:07:48.163Z"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.updatedAt")
                        .doesNotExist());
    }

    @Test
    void getsDeviceById() throws Exception {
        Mockito.when(deviceService.getDevice(ID))
                .thenReturn(new Device()
                        .id(ID)
                        .name("device xyz")
                        .brand("Mac")
                        .state(DeviceState.AVAILABLE)
                        .createdAt(OffsetDateTime.parse("2026-09-19T16:07:48.163Z")));

        mockMvc.perform(MockMvcRequestBuilders.get(DEVICES + "/" + ID))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id")
                        .value(ID.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name")
                        .value("device xyz"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.state")
                        .value("available"));
    }

    @Test
    void returnsNotFoundForUnknownId() throws Exception {
        Mockito.when(deviceService.getDevice(ID))
                .thenThrow(new DeviceNotFoundException(ID));

        mockMvc.perform(MockMvcRequestBuilders.get(DEVICES + "/" + ID))
                .andExpect(MockMvcResultMatchers.status()
                        .isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code")
                        .value("DEVICE_NOT_FOUND"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Device not found for Id: " + ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.path")
                        .value(DEVICES + "/" + ID));
    }

    @Test
    void errorTimestampIsUtc() throws Exception {
        Mockito.when(deviceService.getDevice(ID))
                .thenThrow(new DeviceNotFoundException(ID));

        mockMvc.perform(MockMvcRequestBuilders.get(DEVICES + "/" + ID))
                .andExpect(MockMvcResultMatchers.status()
                        .isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp")
                        .value(Matchers.endsWith("Z")));
    }

    @Test
    void getDevicesWithPageMetadata() throws Exception {
        Mockito.when(deviceService.getDevices(ArgumentMatchers.any(), ArgumentMatchers.any(),
                        ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(),
                        ArgumentMatchers.any()))
                .thenReturn(new DevicePage()
                        .content(List.of(new Device()
                                .id(ID)
                                .name("device xyz")
                                .brand("Mac")
                                .state(DeviceState.AVAILABLE)
                                .createdAt(OffsetDateTime.parse("2026-09-19T16:07:48.163Z"))))
                        .pageNumber(0)
                        .pageSize(20)
                        .totalElements(1L)
                        .totalPages(1));

        mockMvc.perform(MockMvcRequestBuilders.get(DEVICES))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id")
                        .value(ID.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pageNumber")
                        .value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pageSize")
                        .value(20))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements")
                        .value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages")
                        .value(1));
    }

    @Test
    void appliesDefaultsWhenNoPagingGiven() throws Exception {
        Mockito.when(deviceService.getDevices(ArgumentMatchers.any(), ArgumentMatchers.any(),
                        ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(),
                        ArgumentMatchers.any()))
                .thenReturn(new DevicePage().content(Collections.emptyList()));

        mockMvc.perform(MockMvcRequestBuilders.get(DEVICES))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk());

        Mockito.verify(deviceService)
                .getDevices(null, null, 0, 20, DeviceSortField.CREATED_AT, SortDirection.DESC);
    }

    @Test
    void getDeviceWithParameters() throws Exception {
        Mockito.when(deviceService.getDevices(ArgumentMatchers.any(), ArgumentMatchers.any(),
                        ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(),
                        ArgumentMatchers.any()))
                .thenReturn(new DevicePage().content(List.of()));

        mockMvc.perform(MockMvcRequestBuilders.get(DEVICES)
                        .param("brand", "Mac")
                        .param("state", "in-use")
                        .param("page", "2")
                        .param("size", "5")
                        .param("sortBy", "name")
                        .param("sortDirection", "asc"))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk());

        Mockito.verify(deviceService)
                .getDevices("Mac", DeviceState.IN_USE, 2, 5, DeviceSortField.NAME, SortDirection.ASC);
    }

    @Test
    void rejectsUnknownSort() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(DEVICES)
                        .param("sortBy", "unknown"))
                .andExpect(MockMvcResultMatchers.status()
                        .isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code")
                        .value("DATA_INVALID"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Unknown sort field: unknown. Allowed: createdAt, name, brand, state"));

        Mockito.verifyNoInteractions(deviceService);
    }

    @Test
    void rejectsUnknownSortDirection() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(DEVICES)
                        .param("sortDirection", "unknown"))
                .andExpect(MockMvcResultMatchers.status()
                        .isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Unknown sort direction: unknown. Allowed: asc, desc"));

        Mockito.verifyNoInteractions(deviceService);
    }

    @Test
    void rejectsUnknownStateFilter() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(DEVICES)
                        .param("state", "unknown"))
                .andExpect(MockMvcResultMatchers.status()
                        .isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Unknown state value: unknown. Allowed: available, in-use, inactive"));
    }

    @Test
    void rejectsPageSizeWhenNotInAllowedRange() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(DEVICES)
                        .param("size", "0"))
                .andExpect(MockMvcResultMatchers.status()
                        .isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code")
                        .value("DATA_INVALID"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.validationErrors[0].field")
                        .value("size"));

        mockMvc.perform(MockMvcRequestBuilders.get(DEVICES)
                        .param("size", "101"))
                .andExpect(MockMvcResultMatchers.status()
                        .isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.get(DEVICES)
                        .param("page", "-1"))
                .andExpect(MockMvcResultMatchers.status()
                        .isBadRequest());
    }

    @Test
    void deletesDevice() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(DEVICES + "/" + ID))
                .andExpect(MockMvcResultMatchers.status()
                        .isNoContent())
                .andExpect(MockMvcResultMatchers.content()
                        .string(""));

        Mockito.verify(deviceService)
                .deleteDevice(ID);
    }

    @Test
    void notAllowToDeleteDeviceInUse() throws Exception {
        Mockito.doThrow(new DeviceInUseException("Device is in use and cannot be deleted"))
                .when(deviceService)
                .deleteDevice(ID);

        mockMvc.perform(MockMvcRequestBuilders.delete(DEVICES + "/" + ID))
                .andExpect(MockMvcResultMatchers.status()
                        .isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code")
                        .value("DEVICE_IN_USE"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Device is in use and cannot be deleted"));
    }

    @Test
    void returnsNotFoundWhenDeletingUnknownId() throws Exception {
        Mockito.doThrow(new DeviceNotFoundException(ID))
                .when(deviceService)
                .deleteDevice(ID);

        mockMvc.perform(MockMvcRequestBuilders.delete(DEVICES + "/" + ID))
                .andExpect(MockMvcResultMatchers.status()
                        .isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code")
                        .value("DEVICE_NOT_FOUND"));
    }

    @Test
    void rejectsInvalidUuid() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(DEVICES + "/not-a-uuid"))
                .andExpect(MockMvcResultMatchers.status()
                        .isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Invalid value for parameter 'id'"));
    }

    @Test
    void rejectsMissingFields() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(DEVICES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(MockMvcResultMatchers.status()
                        .isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code")
                        .value("DATA_INVALID"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Validation failed"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.path")
                        .value(DEVICES))
                .andExpect(MockMvcResultMatchers.jsonPath("$.validationErrors[*].field")
                        .value(Matchers.containsInAnyOrder("name", "brand")));
    }

    @Test
    void rejectsMalformedJson() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(DEVICES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":"))
                .andExpect(MockMvcResultMatchers.status()
                        .isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Malformed or unreadable JSON request body"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value(Matchers.not(Matchers.containsString("com.example"))));
    }

    @Test
    void rejectsUnknownStateInBody() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(DEVICES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"device xyz","brand":"Mac","state":"bogus"}"""))
                .andExpect(MockMvcResultMatchers.status()
                        .isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Unknown state value: bogus. Allowed: available, in-use, inactive"));
    }

    @Test
    void rejectsWrongContentType() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(DEVICES)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("hello"))
                .andExpect(MockMvcResultMatchers.status()
                        .isUnsupportedMediaType())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code")
                        .value("UNSUPPORTED_MEDIA_TYPE"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.validationErrors")
                        .doesNotExist());
    }

    @Test
    void reportsOptimisticLockAsConflict() throws Exception {
        Mockito.when(deviceService.createDevice(ArgumentMatchers.any(CreateDeviceRequest.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(Device.class, ID));

        mockMvc.perform(MockMvcRequestBuilders.post(DEVICES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PAYLOAD))
                .andExpect(MockMvcResultMatchers.status()
                        .isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code")
                        .value("OPTIMISTIC_LOCK_CONFLICT"));
    }

    @Test
    void reportsDataIntegrityViolationAsConflict() throws Exception {
        Mockito.when(deviceService.createDevice(ArgumentMatchers.any(CreateDeviceRequest.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        mockMvc.perform(MockMvcRequestBuilders.post(DEVICES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PAYLOAD))
                .andExpect(MockMvcResultMatchers.status()
                        .isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code")
                        .value("DATA_INTEGRITY_VIOLATION"));
    }

    @Test
    void hidesUnexpectedFailures() throws Exception {
        Mockito.when(deviceService.createDevice(ArgumentMatchers.any(CreateDeviceRequest.class)))
                .thenThrow(new IllegalStateException("connection pool exhausted at com.example.internals"));

        mockMvc.perform(MockMvcRequestBuilders.post(DEVICES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PAYLOAD))
                .andExpect(MockMvcResultMatchers.status()
                        .isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code")
                        .value("INTERNAL_SERVER_ERROR"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("An unexpected error occurred"));
    }

    @Test
    void updatesDevice() throws Exception {
        Mockito.when(deviceService.updateDevice(ArgumentMatchers.eq(ID),
                        ArgumentMatchers.any(PatchDeviceRequest.class)))
                .thenReturn(new Device()
                        .id(ID)
                        .name("device abc")
                        .brand("Mac")
                        .state(DeviceState.AVAILABLE)
                        .createdAt(OffsetDateTime.parse("2026-09-19T16:07:48.163Z"))
                        .updatedAt(OffsetDateTime.parse("2026-09-20T09:00:00.000Z")));

        mockMvc.perform(MockMvcRequestBuilders.patch(DEVICES + "/" + ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"device abc"}"""))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name")
                        .value("device abc"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.updatedAt")
                        .value("2026-09-20T09:00:00Z"));
    }

    @Test
    void notAllowToUpdateDeviceInUse() throws Exception {
        Mockito.when(deviceService.updateDevice(ArgumentMatchers.eq(ID),
                        ArgumentMatchers.any(PatchDeviceRequest.class)))
                .thenThrow(new DeviceInUseException("Device is in use, name and brand cannot be changed"));

        mockMvc.perform(MockMvcRequestBuilders.patch(DEVICES + "/" + ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"device abc"}"""))
                .andExpect(MockMvcResultMatchers.status()
                        .isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code")
                        .value("DEVICE_IN_USE"));
    }

    @Test
    void updateNotExistDevice() throws Exception {
        Mockito.when(deviceService.updateDevice(ArgumentMatchers.eq(ID),
                        ArgumentMatchers.any(PatchDeviceRequest.class)))
                .thenThrow(new DeviceNotFoundException(ID));

        mockMvc.perform(MockMvcRequestBuilders.patch(DEVICES + "/" + ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"device abc"}"""))
                .andExpect(MockMvcResultMatchers.status()
                        .isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code")
                        .value("DEVICE_NOT_FOUND"));
    }

    @Test
    void rejectsBlankNameOnUpdate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch(DEVICES + "/" + ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":""}"""))
                .andExpect(MockMvcResultMatchers.status()
                        .isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.validationErrors[0].field")
                        .value("name"));

        Mockito.verifyNoInteractions(deviceService);
    }

    @Test
    void rejectsBlankBrandOnUpdate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch(DEVICES + "/" + ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"brand":""}"""))
                .andExpect(MockMvcResultMatchers.status()
                        .isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.validationErrors[0].field")
                        .value("brand"));

        Mockito.verifyNoInteractions(deviceService);
    }
}
