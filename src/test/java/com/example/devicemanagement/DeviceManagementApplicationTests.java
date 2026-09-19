package com.example.devicemanagement;

import com.example.devicemanagement.configuration.AbstractIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DeviceManagementApplicationTests extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
        Assertions.assertTrue(true);
    }

}
