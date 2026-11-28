package com.prontaentrega.controllers;

import com.prontaentrega.persistence.repository.UsuarioRepository;
import com.prontaentrega.services.AuthService;
import com.prontaentrega.utils.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Tag("e2e")
public class ActuatorControllerTest extends AbstractIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private AuthService authService;

    private String getToken() {
        return authService.register("actuator",
                "actuator-test@mail.com",
                "music2000")
            .token();
    }

    @Test
    void healthReturnsOkStatus() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void livenessReturnsOkStatus() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk());
    }

    @Test
    void readinessReturnsOkStatus() throws Exception {
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk());
    }

    @Test
    void infoReturnsForbiddenStatusWhenNotAuthorized() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isForbidden());
    }

    @Test
    void infoReturnsOkStatusWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/actuator/info")
                        .header("Authorization", "Bearer "+getToken()))
                .andExpect(status().isOk());
    }

    @Test
    void metricsReturnsForbiddenStatusWhenNotAuthorized() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isForbidden());
    }

    @Test
    void metricsReturnsOkStatusWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/actuator/metrics")
                        .header("Authorization", "Bearer "+getToken()))
                .andExpect(status().isOk());
    }

    @AfterEach
    void clean() {
        usuarioRepository.deleteAll();
    }
}
