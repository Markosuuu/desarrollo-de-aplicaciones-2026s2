package com.prontaentrega.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.prontaentrega.models.Jugador;
import com.prontaentrega.repository.JugadorRepository;
import com.prontaentrega.services.PlayerCatalogService;
import com.prontaentrega.services.dto.RefreshCatalogResponse;
import com.prontaentrega.services.exceptions.ProviderUnavailableException;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Tests de integracion del refresh usando PostgreSQL real y un servidor HTTP real.
 */
@SpringBootTest
@ActiveProfiles("test")
class PlayerCatalogRefreshIntegrationTest {
    private static final AtomicInteger providerStatus = new AtomicInteger(200);
    private static final AtomicReference<String> providerBody = new AtomicReference<>(validPayload());
    private static HttpServer server;

    @Autowired
    private PlayerCatalogService playerCatalogService;

    @Autowired
    private JugadorRepository jugadorRepository;

    /**
     * Publica la URL del servidor HTTP local antes de crear el contexto Spring.
     */
    @DynamicPropertySource
    static void providerProperties(DynamicPropertyRegistry registry) {
        startServer();
        registry.add("providers.whoscored.url", () -> "http://localhost:" + server.getAddress().getPort() + "/stats");
    }

    /**
     * Limpia PostgreSQL antes de cada caso para probar carga inicial y actualizaciones.
     */
    @BeforeEach
    void cleanDatabase() {
        jugadorRepository.deleteAll();
        providerStatus.set(200);
        providerBody.set(validPayload());
    }

    /**
     * Detiene el servidor HTTP local usado por los tests.
     */
    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    /**
     * Validamos que se persistan en la base los jugadores de WhoScored y que los campos de response sean los valores correctos.
     * */
    @Test
    void refreshShouldPersistPlayers() {
        RefreshCatalogResponse response =
                playerCatalogService.refreshFromWhoScored();

        assertEquals(2, response.created());
        assertEquals(0, response.updated());
        assertEquals(0, response.skipped());

        assertEquals(2, jugadorRepository.count());
        assertTrue(response.success());
        assertEquals("Actualizacion completada correctamente", response.message());
        assertEquals(2, response.created());
        assertEquals(0, response.updated());
        assertEquals(0, response.skipped());
        assertEquals("WhoScored", response.source());
    }

    /**
     * Validamos que al actualizarse un jugador de WhoScored, el cambio se vea persistido y los campos de response lo muestren
     *  como que hubo updates.
     * */
    @Test
    void refreshShouldUpdateExistingPlayers() {
        RefreshCatalogResponse first =
                playerCatalogService.refreshFromWhoScored();

        assertEquals(2, first.created());

        providerBody.set(updatedPayload());

        RefreshCatalogResponse second =
                playerCatalogService.refreshFromWhoScored();

        assertEquals(0, second.created());
        assertEquals(2, second.updated());
        assertEquals(0, second.skipped());
        assertEquals(2, jugadorRepository.count());

        Jugador ana = jugadorRepository.findByWhoscoredId(101)
                .orElseThrow();

        assertEquals(6, ana.getGoles());
        assertEquals(new BigDecimal("8.10"), ana.getRating());
        assertTrue(second.success());
        assertEquals("Actualizacion completada correctamente", second.message());
        assertEquals(0, second.created());
        assertEquals(2, second.updated());
        assertEquals(0, second.skipped());
        assertEquals("WhoScored", second.source());
    }

    /**
     * En caso que los datos de paginacion no esten, se lanza excepcion y no se toca la base de datos.
     * */
    @Test
    void refreshShouldFailWhenProviderPaginationIsInvalid() {
        providerBody.set("""
            {
              "playerTableStats": []
            }
            """);

        ProviderUnavailableException exception = assertThrows(
                ProviderUnavailableException.class,
                () -> playerCatalogService.refreshFromWhoScored()
        );

        assertEquals("WHOSCORED_PAGINACION_INVALIDA", exception.getCode());
        assertEquals(
                "No se pudo completar la actualizacion. WhoScored no informo correctamente la cantidad de paginas.",
                exception.getMessage()
        );

        assertEquals(0, jugadorRepository.count());
    }
    /**
     * Cuando WhoScored responde correctamente, pero todos los registros de jugadores son invalidos, se lanza una excepcion.
     * */
    @Test
    void refreshShouldFailWhenProviderHasNoUsefulData() {
        providerBody.set("""
            {
              "playerTableStats": [
                {
                  "playerId": 101,
                  "name": "Ana",
                  "teamName": null,
                  "tournamentName": "Liga Uno",
                  "age": 24,
                  "height": 170,
                  "weight": 60,
                  "positionText": "Forward",
                  "isActive": null,
                  "goal": 6,
                  "assistTotal": 4,
                  "shotsPerGame": 2.90,
                  "keyPassPerGame": 1.40,
                  "dribbleWonPerGame": 1.90,
                  "foulGivenPerGame": null,
                  "rating": null,
                  "minsPlayed": 950
                }
              ],
              "paging": {
                "currentPage": 1,
                "totalPages": 1,
                "resultsPerPage": 20,
                "totalResults": 1,
                "firstRecordIndex": 0,
                "lastRecordIndex": 0
              }
            }
            """);

        ProviderUnavailableException exception = assertThrows(
                ProviderUnavailableException.class,
                () -> playerCatalogService.refreshFromWhoScored()
        );

        assertEquals("WHOSCORED_SIN_DATOS_UTILES", exception.getCode());
        assertEquals(
                "No se pudo completar la actualizacion. WhoScored no produjo registros utiles.",
                exception.getMessage()
        );

        assertEquals(0, jugadorRepository.count());
    }

    /**
     * Cuando el proveedor WhoScored falla, se lanza una excepcion y el catalogo ya exicstente no se ve modificado.
     * */
    @Test
    void refreshShouldPreserveCatalogWhenProviderFails() {
        jugadorRepository.save(
                Jugador.desdeEstadisticas(
                        999,
                        "Jugador Local",
                        "Equipo Local",
                        "Liga Local",
                        30,
                        180,
                        75,
                        "Midfielder",
                        true,
                        1,
                        1,
                        BigDecimal.ONE,
                        BigDecimal.ONE,
                        BigDecimal.ONE,
                        BigDecimal.ONE,
                        BigDecimal.ONE,
                        90,
                        LocalDateTime.now()
                )
        );

        providerStatus.set(500);
        providerBody.set("{\"error\":\"fallo\"}");

        ProviderUnavailableException exception = assertThrows(
                ProviderUnavailableException.class,
                () -> playerCatalogService.refreshFromWhoScored()
        );

        assertEquals("WHOSCORED_NO_DISPONIBLE", exception.getCode());
        assertEquals(
                "No se pudo completar la actualizacion. Se conserva la informacion local vigente.",
                exception.getMessage()
        );

        assertEquals(1, jugadorRepository.count());

        Jugador jugador = jugadorRepository.findByWhoscoredId(999)
                .orElseThrow();

        assertEquals("Jugador Local", jugador.getNombre());
    }

    /**
     * Cuando WhoScored responde correctamente, pero con registros validos e invalidos, no lanza excepcion, persiste los validos
     * y omite los invalidos, indicandolo en la response.
     * */
    @Test
    void refreshShouldSkipInvalidPlayers() {
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
                  "playerId": 102,
                  "name": "Luis Perez",
                  "teamName": "Azul FC",
                  "tournamentName": "Liga Uno",
                  "age": 28,
                  "height": 180,
                  "weight": 75,
                  "positionText": "Midfielder",
                  "isActive": true,
                  "goal": 2,
                  "assistTotal": 6,
                  "shotsPerGame": 1.30,
                  "keyPassPerGame": 1.50,
                  "dribbleWonPerGame": 0.90,
                  "foulGivenPerGame": 1.10,
                  "rating": 7.20,
                  "minsPlayed": 1200
                },
                {
                  "playerId": null,
                  "name": null,
                  "teamName": "Azul FC",
                  "tournamentName": "Liga Uno",
                  "age": 22,
                  "goal": 1,
                  "assistTotal": 1,
                  "shotsPerGame": 1.00,
                  "keyPassPerGame": 0.50,
                  "dribbleWonPerGame": 0.40,
                  "foulGivenPerGame": 0.50,
                  "rating": 6.50,
                  "minsPlayed": 500
                }
              ],
              "paging": {
                "currentPage": 1,
                "totalPages": 1,
                "resultsPerPage": 20,
                "totalResults": 3,
                "firstRecordIndex": 1,
                "lastRecordIndex": 3
              }
            }
            """);

        RefreshCatalogResponse response =
                playerCatalogService.refreshFromWhoScored();

        assertTrue(response.success());
        assertEquals("Actualizacion completada correctamente", response.message());
        assertEquals(2, response.created());
        assertEquals(0, response.updated());
        assertEquals(1, response.skipped());
        assertEquals("WhoScored", response.source());

        assertEquals(2, jugadorRepository.count());
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

    private static String validPayload() {
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
                },
                {
                  "playerId": 102,
                  "name": "Luis Perez",
                  "teamName": "Azul FC",
                  "tournamentName": "Liga Uno",
                  "age": 28,
                  "height": 180,
                  "weight": 75,
                  "positionText": "Midfielder",
                  "isActive": true,
                  "goal": 2,
                  "assistTotal": 6,
                  "shotsPerGame": 1.30,
                  "keyPassPerGame": 1.50,
                  "dribbleWonPerGame": 0.90,
                  "foulGivenPerGame": 1.10,
                  "rating": 7.20,
                  "minsPlayed": 1200
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
            """;
    }

    private static String updatedPayload() {
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
                  "goal": 6,
                  "assistTotal": 4,
                  "shotsPerGame": 2.90,
                  "keyPassPerGame": 1.40,
                  "dribbleWonPerGame": 1.90,
                  "foulGivenPerGame": 0.70,
                  "rating": 8.10,
                  "minsPlayed": 950
                },
                {
                  "playerId": 102,
                  "name": "Luis Perez",
                  "teamName": "Azul FC",
                  "tournamentName": "Liga Uno",
                  "age": 28,
                  "height": 180,
                  "weight": 75,
                  "positionText": "Midfielder",
                  "isActive": true,
                  "goal": 3,
                  "assistTotal": 7,
                  "shotsPerGame": 1.50,
                  "keyPassPerGame": 1.70,
                  "dribbleWonPerGame": 1.00,
                  "foulGivenPerGame": 1.00,
                  "rating": 7.40,
                  "minsPlayed": 1250
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
            """;
    }
}
