package com.example.devicemanagement.controller;

import java.time.OffsetDateTime;
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
        Mockito.doThrow(new DeviceInUseException(ID))
                .when(deviceService)
                .deleteDevice(ID);

        mockMvc.perform(MockMvcRequestBuilders.delete(DEVICES + "/" + ID))
                .andExpect(MockMvcResultMatchers.status()
                        .isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code")
                        .value("DEVICE_IN_USE"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Device is in use and cannot be deleted: " + ID));
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
}
