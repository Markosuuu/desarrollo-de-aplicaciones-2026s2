package com.prontaentrega;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.prontaentrega.utils.AbstractIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Tag("integracion")
class AppTest extends AbstractIntegrationTest {
    @Test
    void contextLoads() {
        assertTrue(true);
    }
}
