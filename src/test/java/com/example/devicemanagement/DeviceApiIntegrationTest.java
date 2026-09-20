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
    void deletesDevice() throws Exception {
        String id = createDevice("""
                {"name":"device xyz","brand":"Mac","state":"available"}""");

        mockMvc.perform(MockMvcRequestBuilders.delete(DEVICES + "/" + id))
                .andExpect(MockMvcResultMatchers.status()
                        .isNoContent());

        Assertions.assertThat(deviceRepository.count())
                .isZero();
        mockMvc.perform(MockMvcRequestBuilders.get(DEVICES + "/" + id))
                .andExpect(MockMvcResultMatchers.status()
                        .isNotFound());
    }

    @Test
    void notAllowToDeleteDeviceInUse() throws Exception {
        String id = createDevice("""
                {"name":"device xyz","brand":"Mac","state":"in-use"}""");

        mockMvc.perform(MockMvcRequestBuilders.delete(DEVICES + "/" + id))
                .andExpect(MockMvcResultMatchers.status()
                        .isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code")
                        .value("DEVICE_IN_USE"));

        Assertions.assertThat(deviceRepository.count())
                .isOne();
    }

    @Test
    void returnsNotFoundWhenDeletingUnknownId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(DEVICES + "/" + UUID.randomUUID()))
                .andExpect(MockMvcResultMatchers.status()
                        .isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code")
                        .value("DEVICE_NOT_FOUND"));
    }

    @Test
    void returnsNotFoundForUnknownId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(DEVICES + "/" + UUID.randomUUID()))
                .andExpect(MockMvcResultMatchers.status()
                        .isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code")
                        .value("DEVICE_NOT_FOUND"));
    }

    @Test
    void getDevicesNewestFirstByDefault() throws Exception {
        createDevice("""
                {"name":"first","brand":"Mac","state":"available"}""");
        createDevice("""
                {"name":"second","brand":"Dell","state":"in-use"}""");
        createDevice("""
                {"name":"third","brand":"Mac","state":"inactive"}""");

        mockMvc.perform(MockMvcRequestBuilders.get(DEVICES))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()")
                        .value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name")
                        .value("third"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements")
                        .value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages")
                        .value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pageNumber")
                        .value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pageSize")
                        .value(20));
    }

    @Test
    void filtersByBrand() throws Exception {
        createDevice("""
                {"name":"first","brand":"Mac","state":"available"}""");
        createDevice("""
                {"name":"second","brand":"Dell","state":"in-use"}""");

        mockMvc.perform(MockMvcRequestBuilders.get(DEVICES)
                        .param("brand", "Mac"))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()")
                        .value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements")
                        .value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].brand")
                        .value("Mac"));
    }

    @Test
    void filtersByState() throws Exception {
        createDevice("""
                {"name":"first","brand":"Mac","state":"available"}""");
        createDevice("""
                {"name":"second","brand":"Dell","state":"in-use"}""");

        mockMvc.perform(MockMvcRequestBuilders.get(DEVICES)
                        .param("state", "in-use"))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()")
                        .value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements")
                        .value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name")
                        .value("second"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].brand")
                        .value("Dell"));
    }

    @Test
    void filtersByBrandAndState() throws Exception {
        createDevice("""
                {"name":"first","brand":"Mac","state":"available"}""");
        createDevice("""
                {"name":"second","brand":"Mac","state":"in-use"}""");
        createDevice("""
                {"name":"third","brand":"Dell","state":"in-use"}""");

        mockMvc.perform(MockMvcRequestBuilders.get(DEVICES)
                        .param("brand", "Mac")
                        .param("state", "in-use"))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements")
                        .value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name")
                        .value("second"));
    }

    @Test
    void checkSecondPage() throws Exception {
        for (int i = 0; i < 5; i++) {
            createDevice("""
                    {"name":"device %d","brand":"Mac","state":"available"}""".formatted(i));
        }

        mockMvc.perform(MockMvcRequestBuilders.get(DEVICES)
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()")
                        .value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name")
                        .value("device 2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pageNumber")
                        .value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pageSize")
                        .value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements")
                        .value(5))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages")
                        .value(3));
    }

    @Test
    void sortsByNameAsc() throws Exception {
        createDevice("""
                {"name":"iphone","brand":"Mac","state":"available"}""");
        createDevice("""
                {"name":"dou phone","brand":"Mac","state":"available"}""");
        createDevice("""
                {"name":"macbook","brand":"Mac","state":"available"}""");

        mockMvc.perform(MockMvcRequestBuilders.get(DEVICES)
                        .param("sortBy", "name")
                        .param("sortDirection", "asc"))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name")
                        .value("dou phone"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].name")
                        .value("iphone"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].name")
                        .value("macbook"));
    }

    @Test
    void returnsEmptyPageWhenNothingMatches() throws Exception {
        createDevice("""
                {"name":"first","brand":"Mac","state":"available"}""");

        mockMvc.perform(MockMvcRequestBuilders.get(DEVICES)
                        .param("brand", "xyz"))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()")
                        .value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements")
                        .value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages")
                        .value(0));
    }

    private String createDevice(String payload) throws Exception {
        String created = mockMvc.perform(MockMvcRequestBuilders.post(DEVICES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(MockMvcResultMatchers.status()
                        .isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(created, "$.id");
    }
}
