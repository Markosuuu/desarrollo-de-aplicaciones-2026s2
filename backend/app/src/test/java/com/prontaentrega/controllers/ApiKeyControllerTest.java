package com.prontaentrega.controllers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prontaentrega.persistence.repository.dao.ApiKeyDAO;
import com.prontaentrega.services.ApiKeyService;
import com.prontaentrega.utils.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiKeyControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApiKeyDAO apiKeyDAO;

    @Autowired
    private ApiKeyService apiKeyService;

    @Value("${api-key.master-secret}")
    private String masterSecret;

    @BeforeEach
    void setUp() {
        apiKeyDAO.deleteAll();
    }

    /**
     * Se verifica que una solicitud con la Master Key correcta genere una API Key,
     * responda con HTTP 201 Created y permita validar la clave generada.
     */
    @Test
    void generar_conMasterKeyValida_deberiaCrearApiKey() throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/keys")
                        .header("X-Master-Key", masterSecret)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "nombre": "Sistema de prueba"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.apiKey").isNotEmpty())
                .andReturn();

        String respuesta = resultado.getResponse().getContentAsString();
        String apiKey = new ObjectMapper()
                .readTree(respuesta)
                .get("apiKey")
                .asText();

        assertTrue(apiKeyService.validar(apiKey));
        assertEquals(1, apiKeyDAO.count());
    }

    /**
     * Se verifica que una solicitud con una Master Key incorrecta sea rechazada
     * con HTTP 403 Forbidden y no genere ningún registro.
     */
    @Test
    void generar_conMasterKeyInvalida_deberiaRechazarSolicitud() throws Exception {
        mockMvc.perform(post("/api/keys")
                        .header("X-Master-Key", "clave-incorrecta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "nombre": "Sistema de prueba"
                                }
                                """))
                .andExpect(status().isForbidden());

        assertEquals(0, apiKeyDAO.count());
    }

    /**
     * Se verifica que una solicitud sin la cabecera X-Master-Key sea rechazada
     * por no proporcionar el dato obligatorio.
     */
    @Test
    void generar_sinMasterKey_deberiaRechazarSolicitud() throws Exception {
        mockMvc.perform(post("/api/keys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "nombre": "Sistema de prueba"
                                }
                                """))
                .andExpect(status().isBadRequest());

        assertEquals(0, apiKeyDAO.count());
    }
}
