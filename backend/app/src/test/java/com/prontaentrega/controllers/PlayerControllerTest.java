package com.prontaentrega.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.prontaentrega.models.Jugador;
import com.prontaentrega.repository.JugadorRepository;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests end-to-end HTTP del controller de jugadores con MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlayerControllerTest {
    private static final AtomicInteger providerStatus = new AtomicInteger(200);
    private static final AtomicReference<String> providerBody = new AtomicReference<>(payload());
    private static HttpServer server;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JugadorRepository jugadorRepository;

    /**
     * Registra la URL del proveedor HTTP local para el contexto de Spring.
     */
    @DynamicPropertySource
    static void providerProperties(DynamicPropertyRegistry registry) {
        startServer();
        registry.add("providers.whoscored.url", () -> "http://localhost:" + server.getAddress().getPort() + "/stats");
    }

    /**
     * Limpia datos persistidos antes de cada test HTTP.
     */
    @BeforeEach
    void cleanDatabase() {
        jugadorRepository.deleteAll();
        providerStatus.set(200);
        providerBody.set(payload());
    }

    /**
     * Detiene el proveedor HTTP local de pruebas.
     */
    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    /**
     * Verifica que la consulta de catalogo sea publica y paginada.
     */
    @Test
    void catalogShouldBePublicAndPaginated() throws Exception {
        mockMvc.perform(get("/api/players").param("liga", "La Liga").param("page", "1").param("per_page", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jugadores").isArray())
                .andExpect(jsonPath("$.paginacion.total").isNumber());
    }

    /**
     * Verifica que la actualizacion publica responda con el contrato exitoso.
     */
    @Test
    void updateShouldBePublicAndReturnSuccessContract() throws Exception {
        mockMvc.perform(post("/api/players/update"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Actualizacion completada correctamente"))
                .andExpect(jsonPath("$.created").value(1))
                .andExpect(jsonPath("$.updated").value(0))
                .andExpect(jsonPath("$.skipped").value(0))
                .andExpect(jsonPath("$.source").value("WhoScored"));
    }

    /**
     * Verifica que una falla de proveedor responda como Bad Gateway con datos locales preservados.
     */
    @Test
    void updateShouldReturnBadGatewayWhenProviderFails() throws Exception {
        providerStatus.set(500);
        providerBody.set("{\"error\":\"fallo\"}");

        mockMvc.perform(post("/api/players/update"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("No se pudo completar la actualizacion. Se conserva la informacion local vigente."))
                .andExpect(jsonPath("$.created").value(0))
                .andExpect(jsonPath("$.updated").value(0))
                .andExpect(jsonPath("$.skipped").value(0))
                .andExpect(jsonPath("$.source").value("WhoScored"));
    }

    /**
     * Verifica que una falla de proveedor relacionada a la paginación responda correctamente como Bad GateWay.
     * */
    @Test
    void updateShouldReturnBadGatewayWhenPaginationIsInvalid() throws Exception {
        providerBody.set("""
            {
              "playerTableStats": []
            }
            """);

        mockMvc.perform(post("/api/players/update"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(
                        "No se pudo completar la actualizacion. WhoScored no informo correctamente la cantidad de paginas."
                ))
                .andExpect(jsonPath("$.created").value(0))
                .andExpect(jsonPath("$.updated").value(0))
                .andExpect(jsonPath("$.skipped").value(0))
                .andExpect(jsonPath("$.source").value("WhoScored"));
    }

    /**
     * Verifica que en caso de que el proveedor responda correctamente pero sin data util, responda con Bad Gatewaty.
     */
    @Test
    void updateShouldReturnBadGatewayWhenProviderHasNoUsefulData() throws Exception {
        providerBody.set("""
            {
              "playerTableStats": [],
              "paging": {
                "currentPage": 1,
                "totalPages": 1,
                "resultsPerPage": 20,
                "totalResults": 0,
                "firstRecordIndex": 0,
                "lastRecordIndex": 0
              }
            }
            """);

        mockMvc.perform(post("/api/players/update"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(
                        "No se pudo completar la actualizacion. WhoScored no produjo registros utiles."
                ))
                .andExpect(jsonPath("$.created").value(0))
                .andExpect(jsonPath("$.updated").value(0))
                .andExpect(jsonPath("$.skipped").value(0))
                .andExpect(jsonPath("$.source").value("WhoScored"));
    }

    /**
     * Verifica que, en caso de que se omitan jugadores por ser invalidos, quede expresado en el body de la response
     * con un status 200.
     */
    @Test
    void updateShouldReturnSkippedPlayersInResponse() throws Exception {
        providerBody.set("""
            {
              "playerTableStats": [
                {
                  "playerId": 101,
                  "name": "Ana Gomez",
                  "teamName": "Rojo FC",
                  "tournamentName": "Liga Uno",
                  "age": 24,
                  "height": 170,
                  "weight": 60,
                  "positionText": "Forward",
                  "isActive": true,
                  "goal": 5,
                  "assistTotal": 3,
                  "shotsPerGame": 2.50,
                  "keyPassPerGame": 1.20,
                  "dribbleWonPerGame": 1.70,
                  "foulGivenPerGame": 0.80,
                  "rating": 7.90,
                  "minsPlayed": 900
                },
                {
                  "playerId": null,
                  "name": "Jugador Incompleto",
                  "teamName": "Rojo FC",
                  "tournamentName": "Liga Uno"
                }
              ],
              "paging": {
                "currentPage": 1,
                "totalPages": 1,
                "resultsPerPage": 20,
                "totalResults": 2,
                "firstRecordIndex": 1,
                "lastRecordIndex": 2
              }
            }
            """);

        mockMvc.perform(post("/api/players/update"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(
                        "Actualizacion completada correctamente"
                ))
                .andExpect(jsonPath("$.created").value(1))
                .andExpect(jsonPath("$.updated").value(0))
                .andExpect(jsonPath("$.skipped").value(1))
                .andExpect(jsonPath("$.source").value("WhoScored"));
    }

    /**
    * Verifica la actualización de jugadores existentes, indicando una response de status 200 y la cantidad de
     * jugadores actualizados en la response
    * */
    @Test
    void updateShouldReturnUpdatedPlayersInResponse() throws Exception {
        jugadorRepository.save(
                Jugador.desdeEstadisticas(
                        101,
                        "Ana Gomez",
                        "Rojo FC",
                        "Liga Uno",
                        24,
                        170,
                        60,
                        "Forward",
                        true,
                        1,
                        1,
                        BigDecimal.ONE,
                        BigDecimal.ONE,
                        BigDecimal.ONE,
                        BigDecimal.ONE,
                        BigDecimal.ONE,
                        100,
                        LocalDateTime.now()
                )
        );

        mockMvc.perform(post("/api/players/update"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(
                        "Actualizacion completada correctamente"
                ))
                .andExpect(jsonPath("$.created").value(0))
                .andExpect(jsonPath("$.updated").value(1))
                .andExpect(jsonPath("$.skipped").value(0))
                .andExpect(jsonPath("$.source").value("WhoScored"));
    }


    private static void startServer() {
        if (server != null) {
            return;
        }

        try {
            server = HttpServer.create(new InetSocketAddress(0), 0);
            server.createContext("/stats", exchange -> {
                byte[] body = providerBody.get().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(providerStatus.get(), body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
            });
            server.setExecutor(Executors.newSingleThreadExecutor());
            server.start();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo iniciar el proveedor local de prueba", ex);
        }
    }

    private static String payload() {
        return """
            {
              "playerTableStats": [
                {
                  "playerId": 101,
                  "name": "Ana Gomez",
                  "teamName": "Rojo FC",
                  "tournamentName": "Liga Uno",
                  "age": 24,
                  "height": 170,
                  "weight": 60,
                  "positionText": "Forward",
                  "isActive": true,
                  "goal": 5,
                  "assistTotal": 3,
                  "shotsPerGame": 2.50,
                  "keyPassPerGame": 1.20,
                  "dribbleWonPerGame": 1.70,
                  "foulGivenPerGame": 0.80,
                  "rating": 7.90,
                  "minsPlayed": 900
                }
              ],
              "paging": {
                "currentPage": 1,
                "totalPages": 1,
                "resultsPerPage": 20,
                "totalResults": 1,
                "firstRecordIndex": 1,
                "lastRecordIndex": 1
              }
            }
            """;
    }
}
