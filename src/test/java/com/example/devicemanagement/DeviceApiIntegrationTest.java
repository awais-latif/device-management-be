package com.example.devicemanagement;

import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.example.devicemanagement.configuration.AbstractIntegrationTest;
import com.example.devicemanagement.enums.DeviceState;
import com.example.devicemanagement.model.DeviceEntity;
import com.example.devicemanagement.repository.DeviceEntityRepository;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class DeviceApiIntegrationTest extends AbstractIntegrationTest {

    private static final String DEVICES = "/api/v1/devices";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeviceEntityRepository deviceRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearDevices() {
        deviceRepository.deleteAll();
    }

    @Test
    void createNewDevice() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(DEVICES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"device xyz","brand":"Mac","state":"in-use"}"""))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.state").value("in-use"));

        Assertions.assertThat(deviceRepository.findAll()).singleElement().satisfies(saved -> {
            Assertions.assertThat(saved.getId()).isNotNull();
            Assertions.assertThat(saved.getName()).isEqualTo("device xyz");
            Assertions.assertThat(saved.getBrand()).isEqualTo("Mac");
            Assertions.assertThat(saved.getState()).isEqualTo(DeviceState.IN_USE);
            Assertions.assertThat(saved.getCreatedAt()).isNotNull();
            Assertions.assertThat(saved.getVersion()).isZero();
            Assertions.assertThat(saved.getUpdatedAt()).isNull();
        });
    }

    @Test
    void defaultsStateToAvailable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(DEVICES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"device xyz","brand":"Mac"}"""))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.state").value("available"));

        Assertions.assertThat(deviceRepository.findAll())
                .singleElement()
                .extracting(DeviceEntity::getState)
                .isEqualTo(DeviceState.AVAILABLE);
    }

   @Test
    void saveEnumNameInTable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(DEVICES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"device xyz","brand":"Mac","state":"in-use"}"""))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        String storedState = jdbcTemplate.queryForObject("select state from device", String.class);

        Assertions.assertThat(storedState).isEqualTo("IN_USE");
    }

    @Test
    void createNothingWhenPayloadIsInvalid() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(DEVICES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Assertions.assertThat(deviceRepository.count()).isZero();
    }

    @Test
    void getDeviceById() throws Exception {
        String created = mockMvc.perform(MockMvcRequestBuilders.post(DEVICES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"device xyz","brand":"Mac","state":"in-use"}"""))
                .andExpect(MockMvcResultMatchers.status()
                        .isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = JsonPath.read(created, "$.id");

        mockMvc.perform(MockMvcRequestBuilders.get(DEVICES + "/" + id))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id")
                        .value(id))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name")
                        .value("device xyz"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.brand")
                        .value("Mac"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.state")
                        .value("in-use"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt")
                        .isNotEmpty());
    }

    @Test
    void returnsNotFoundForUnknownId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(DEVICES + "/" + UUID.randomUUID()))
                .andExpect(MockMvcResultMatchers.status()
                        .isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code")
                        .value("DEVICE_NOT_FOUND"));
    }
}
