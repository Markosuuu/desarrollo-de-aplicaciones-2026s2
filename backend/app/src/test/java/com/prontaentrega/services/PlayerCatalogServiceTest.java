package com.prontaentrega.services;

import com.prontaentrega.models.Jugador;
import com.prontaentrega.repository.JugadorRepository;
import com.prontaentrega.controllers.dtos.CatalogResponse;
import com.prontaentrega.utils.AbstractIntegrationTest;
import org.springframework.data.domain.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
public class PlayerCatalogServiceTest extends AbstractIntegrationTest {
    @Autowired
    private PlayerCatalogService playerCatalogService;

    @Autowired
    private JugadorRepository jugadorRepository;

    @BeforeEach
    void clean() {
        jugadorRepository.deleteAll();
    }

    /**
     ---> Metodo helper para no hacer setup tan largo antes de cada test <---
    **/
    private Jugador saveJugador(Integer whoscoredId, String nombre, String equipo, String liga) {
        Jugador jugador = Jugador.desdeEstadisticas(whoscoredId, nombre, equipo, liga,
                25, 180, 75, "MC", true, 10, 5,
                BigDecimal.valueOf(2.5), BigDecimal.valueOf(1.2), BigDecimal.valueOf(1.5),
                BigDecimal.valueOf(0.5), BigDecimal.valueOf(7.5), 2000, LocalDateTime.now()
        );
        return jugadorRepository.save(jugador);
    }

    /**
     ---> Metodo helper para cargar muchos jugadores en la base <---
     **/
    private void saveManyJugadores(int cantidad) {
        for (int i = 1; i <= cantidad; i++) {
            saveJugador(
                    i,
                    "Jugador " + i,
                    "Equipo " + i,
                    "Liga " + i
            );
        }
    }

    /** -------------------------------------------------------------------------------**/
    /** ------------------------------- TESTS DE SEARCH -------------------------------**/
    /** -------------------------------------------------------------------------------**/

    /** Se verifica que la búsqueda sin filtros devuelva todos los jugadores del catálogo. */
    @Test
    void searchShouldReturnAllPlayersWhenNoFiltersAreProvided() {
        saveJugador(1, "Lionel Messi", "Inter Miami", "MLS");
        saveJugador(2, "Enzo Fernandez", "Chelsea", "Premier League");

        Page<Jugador> response = playerCatalogService.search(
                null,
                null,
                null,
                1,
                10
        );

        assertEquals(2, response.getTotalElements());
        assertEquals(2, response.stream().count());
    }

    /** Se verifica que la búsqueda devuelva los jugadores que coinciden con el equipo indicado. */
    @Test
    void searchShouldReturnPlayersMatchingTeam() {
        saveJugador(1, "Lionel Messi", "Inter Miami", "MLS");
        saveJugador(2, "Enzo Fernandez", "Chelsea", "Premier League");

        Page<Jugador> response = playerCatalogService.search(
                null,
                "Inter Miami",
                null,
                1,
                10
        );
        assertEquals(1, response.getTotalElements());
        assertEquals("Lionel Messi", response.stream().toList().getFirst().getNombre());
    }

    /** Se verifica que la búsqueda devuelva los jugadores que coinciden con el nombre indicado. */
    @Test
    void searchShouldReturnPlayersMatchingName() {
        saveJugador(1, "Lionel Messi", "Inter Miami", "MLS");
        saveJugador(2, "Enzo Fernandez", "Chelsea", "Premier League");

        Page<Jugador> response = playerCatalogService.search(
                null,
                null,
                "Messi",
                1,
                10
        );

        assertEquals(1, response.stream().count());
        assertEquals("Lionel Messi", response.stream().toList().getFirst().getNombre());
    }

    /** Se verifica que la búsqueda devuelva los jugadores que coinciden con la liga indicada. */
    @Test
    void searchShouldReturnPlayersMatchingLiga() {
        saveJugador(1, "Lionel Messi", "Inter Miami", "MLS");
        saveJugador(2, "Enzo Fernandez", "Chelsea", "Premier League");

        Page<Jugador> response = playerCatalogService.search(
                "MLS    ",
                null,
                null,
                1,
                10
        );

        assertEquals(1, response.stream().count());
        assertEquals("Lionel Messi", response.stream().toList().getFirst().getNombre());
    }

    /** Se verifica que la búsqueda aplique correctamente múltiples filtros al mismo tiempo. */
    @Test
    void searchShouldReturnPlayersMatchingAllFilters() {
        saveJugador(1, "Lionel Messi", "Inter Miami", "MLS");
        saveJugador(2, "Eder Aller", "Barcelona", "La Liga");
        saveJugador(3, "Luis Suarez", "Inter Miami", "MLS");

        Page<Jugador> response = playerCatalogService.search(
                "MLS",
                "Inter Miami",
                "Messi",
                1,
                10
        );

        assertEquals(1, response.stream().count());
        assertEquals("Lionel Messi", response.stream().toList().getFirst().getNombre());
        assertEquals("Inter Miami", response.stream().toList().getFirst().getEquipo());
        assertEquals("MLS", response.stream().toList().getFirst().getLiga());
    }

    /** Se verifica que la búsqueda normalice los filtros antes de consultar el catálogo. */
    @Test
    void searchShouldNormalizeFilters() {
        saveJugador(1, "Lionel Messi", "Inter Miami", "MLS");
        saveJugador(2, "Enzo Fernandez", "Chelsea", "Premier League");

        Page<Jugador> response = playerCatalogService.search(
                "  MLS  ",
                "  Inter Miami  ",
                "  Messi  ",
                1,
                10
        );

        assertEquals(1, response.stream().count());
        assertEquals("Lionel Messi", response.stream().toList().getFirst().getNombre());
    }

    /** Se verifica que la búsqueda respete la cantidad de jugadores solicitada por página. */
    @Test
    void searchShouldReturnRequestedPlayersPerPage() {
        saveManyJugadores(3);

        Page<Jugador> response = playerCatalogService.search(
                null,
                null,
                null,
                1,
                2
        );

        assertEquals(2, response.stream().count());
        assertEquals("Jugador 1", response.stream().toList().getFirst().getNombre());
        assertEquals("Jugador 2", response.stream().toList().get(1).getNombre());
        assertEquals(2, response.getSize());
        assertEquals(3, response.getTotalElements());
        assertEquals(2, response.getTotalPages());
    }

    /** Se verifica que la búsqueda devuelva correctamente los jugadores correspondientes a una página específica. */
    @Test
    void searchShouldReturnPlayersFromRequestedPage() {
        saveManyJugadores(3);

        Page<Jugador> response = playerCatalogService.search(
                null,
                null,
                null,
                2,
                2
        );

        assertEquals(1, response.getNumberOfElements());
        assertEquals("Jugador 3", response.stream().toList().getFirst().getNombre());
        assertEquals(1, response.getNumber());
        assertEquals(1, response.getNumberOfElements());
        assertEquals(3, response.getTotalElements());
        assertEquals(2, response.getTotalPages());
    }

    /** Se verifica que una búsqueda sin coincidencias devuelva una respuesta vacía sin generar un error. */
    @Test
    void searchShouldReturnEmptyResponseWhenNoPlayersMatch() {
        saveJugador(1, "Lionel Messi", "Inter Miami", "MLS");

        Page<Jugador> response = playerCatalogService.search(
                null,
                null,
                "Jugador inexistente",
                1,
                10
        );

        assertTrue(response.stream().toList().isEmpty());
        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getTotalPages());
    }

    /** Se verifica que una página menor a uno se interprete como la primera página. */
    @Test
    void searchShouldUseFirstPageWhenPageIsLessThanOne() {
        saveManyJugadores(3);

        Page<Jugador> response = playerCatalogService.search(
                null,
                null,
                null,
                0,
                2
        );

        assertEquals(2, response.getNumberOfElements());
        assertEquals("Jugador 1", response.getContent().getFirst().getNombre());
        assertEquals(0, response.getNumber());
    }

    /** TEST DE LIMITES: Se verifica que una cantidad de jugadores por página mayor a 50 se limite a 50. */
    @Test
    void searchShouldLimitPerPageToFifty() {
        saveManyJugadores(51);

        Page<Jugador> response = playerCatalogService.search(
                null,
                null,
                null,
                1,
                51
        );

        assertEquals(50, response.getNumberOfElements());
        assertEquals(50, response.getSize());
        assertEquals(51, response.getTotalElements());
        assertEquals(2, response.getTotalPages());
    }

    /** TEST DE LIMITES: Se verifica que una cantidad de jugadores por página igual a 50 retorne 50. */
    @Test
    void searchFiftyPerPageShouldReturnFifty() {
        saveManyJugadores(51);

        Page<Jugador> response = playerCatalogService.search(
                null,
                null,
                null,
                1,
                50
        );

        assertEquals(50, response.getNumberOfElements());
        assertEquals(50, response.getSize());
        assertEquals(51, response.getTotalElements());
        assertEquals(2, response.getTotalPages());
    }

    /** TEST DE LIMITES: Se verifica que una cantidad de jugadores por página igual a 49 retorne 49. */
    @Test
    void search49PerPageShouldReturn49Elements() {
        saveManyJugadores(51);

        Page<Jugador> response = playerCatalogService.search(
                null,
                null,
                null,
                1,
                49
        );

        assertEquals(49, response.getNumberOfElements());
        assertEquals(49, response.getSize());
        assertEquals(51, response.getTotalElements());
        assertEquals(2, response.getTotalPages());
    }

    /** TEST DE LIMITES: Se verifica que una cantidad de jugadores por página menor a uno se convierta en uno. */
    @Test
    void searchShouldUseOneWhenPerPageIsLessThanOne() {
        saveManyJugadores(3);

        Page<Jugador> response = playerCatalogService.search(
                null,
                null,
                null,
                1,
                0
        );

        assertEquals(1, response.getNumberOfElements());
        assertEquals(1, response.getSize());
        assertEquals(3, response.getTotalElements());
        assertEquals(3, response.getTotalPages());
    }
}
