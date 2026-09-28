package com.prontaentrega.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios de dominio para las invariantes de Jugador.
 */
class JugadorTest {
    @Test
    void actualizarEstadisticasShouldReplaceSportValues() {
        Jugador jugador = Jugador.desdeEstadisticas(
                101,
                "Ana Gomez",
                "Rojo FC",
                "Liga Uno",
                20,
                170,
                60,
                "Forward",
                true,
                1,
                2,
                new BigDecimal("1.5"),
                new BigDecimal("1.0"),
                new BigDecimal("2.0"),
                new BigDecimal("0.5"),
                new BigDecimal("7.1"),
                500,
                LocalDateTime.now()
        );

        jugador.actualizarEstadisticas(
                21,
                171,
                61,
                "Midfielder",
                false,
                3,
                4,
                new BigDecimal("2.5"),
                new BigDecimal("2.0"),
                new BigDecimal("3.0"),
                new BigDecimal("1.0"),
                new BigDecimal("7.8"),
                700,
                LocalDateTime.now(),
                "WhoScored"
        );

        assertEquals(21, jugador.getEdad());
        assertEquals(171, jugador.getAltura());
        assertEquals(61, jugador.getPeso());
        assertEquals("Midfielder", jugador.getPosicion());
        assertFalse(jugador.getActivo());
        assertEquals(3, jugador.getGoles());
        assertEquals(4, jugador.getAsistencias());
        assertEquals(new BigDecimal("2.5"), jugador.getDisparosPorPartido());
        assertEquals(new BigDecimal("2.0"), jugador.getKeyPassesPorPartido());
        assertEquals(new BigDecimal("3.0"), jugador.getDribblesGanadosPorPartido());
        assertEquals(new BigDecimal("1.0"), jugador.getFaltasCometidasPorPartido());
        assertEquals(new BigDecimal("7.8"), jugador.getRating());
        assertEquals(700, jugador.getMinutosJugados());

        // Estos valores no deberían modificarse al actualizar estadísticas.
        assertEquals(new BigDecimal("1"), jugador.getCotizacionActual());
        assertEquals(100, jugador.getTokensRestantes());
    }

    @Test
    void desdeEstadisticasShouldRejectBlankIdentity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Jugador.desdeEstadisticas(
                        101,
                        " ",
                        "Rojo FC", "Liga Uno", 20,
                        170, 60, "Forward", true, 1,
                        2, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                        500, LocalDateTime.now()
                )
        );
    }

    @Test
    void desdeEstadisticasShouldRejectNegativeGoalsStats() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Jugador.desdeEstadisticas(
                        101, "Ana Gomez", "Rojo FC", "Liga Uno", 20,
                        170, 60, "Forward", true,
                        -1,
                        2, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                        500, LocalDateTime.now()
                )
        );
    }
    /**
     * Habria que luego añadir mas tests como el de arriba pero para cada caso de un valor negativo invalido al llamar a desdeEstadisticas.
     * */


    @Test
    void actualizarEstadisticasShouldReplaceAllStatValues() {
        Jugador jugador = Jugador.desdeEstadisticas(101, "Ana Gomez",
                "Rojo FC", "Liga Uno", 20, 170, 60, "Forward",
                true, 1, 2, new BigDecimal("1.5"),
                new BigDecimal("1.0"), new BigDecimal("2.0"), new BigDecimal("0.5"),
                new BigDecimal("7.1"), 500, LocalDateTime.now()
        );

        jugador.actualizarEstadisticas(21, 171, 61, "Midfielder", false,
                10, 8, new BigDecimal("3.5"), new BigDecimal("2.5"),
                new BigDecimal("4.0"), new BigDecimal("1.5"), new BigDecimal("8.9"),
                1500, LocalDateTime.now(), "WhoScored"
        );

        assertEquals(21, jugador.getEdad());
        assertEquals(171, jugador.getAltura());
        assertEquals(61, jugador.getPeso());
        assertEquals("Midfielder", jugador.getPosicion());
        assertFalse(jugador.getActivo());

        assertEquals(10, jugador.getGoles());
        assertEquals(8, jugador.getAsistencias());
        assertEquals(new BigDecimal("3.5"), jugador.getDisparosPorPartido());
        assertEquals(new BigDecimal("2.5"), jugador.getKeyPassesPorPartido());
        assertEquals(new BigDecimal("4.0"), jugador.getDribblesGanadosPorPartido());
        assertEquals(new BigDecimal("1.5"), jugador.getFaltasCometidasPorPartido());
        assertEquals(new BigDecimal("8.9"), jugador.getRating());
        assertEquals(1500, jugador.getMinutosJugados());
    }

    @Test
    void actualizarEstadisticasShouldPreserveMarketValues() {
        Jugador jugador = Jugador.desdeEstadisticas(
                101, "Ana Gomez", "Rojo FC", "Liga Uno", 20, 170,
                60, "Forward", true, 1, 2, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, 500, LocalDateTime.now()
        );

        jugador.actualizarEstadisticas(
                21, 171, 61, "Midfielder", false, 10, 8,
                new BigDecimal("3.5"), new BigDecimal("2.5"), new BigDecimal("4.0"), new BigDecimal("1.5"),
                new BigDecimal("8.9"), 1500, LocalDateTime.now(), "WhoScored"
        );

        assertEquals(new BigDecimal("1"), jugador.getCotizacionActual());
        assertEquals(100, jugador.getTokensRestantes());
    }

}
