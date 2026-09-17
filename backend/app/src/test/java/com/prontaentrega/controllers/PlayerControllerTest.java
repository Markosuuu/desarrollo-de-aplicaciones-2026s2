package com.prontaentrega.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void catalogShouldBePublicAndPaginated() throws Exception {
        mockMvc.perform(get("/api/players").param("liga", "La Liga").param("page", "1").param("per_page", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jugadores").isArray())
                .andExpect(jsonPath("$.paginacion.total").isNumber());
    }
}
