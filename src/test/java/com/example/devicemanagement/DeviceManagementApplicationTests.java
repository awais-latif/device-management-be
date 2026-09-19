package com.example.devicemanagement;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import com.example.devicemanagement.configuration.AbstractIntegrationTest;
import com.example.devicemanagement.controller.DeviceController;

@SpringBootTest
class DeviceManagementApplicationTests extends AbstractIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        Assertions.assertThat(applicationContext.getBean(DeviceController.class))
                .isNotNull();
    }
}
