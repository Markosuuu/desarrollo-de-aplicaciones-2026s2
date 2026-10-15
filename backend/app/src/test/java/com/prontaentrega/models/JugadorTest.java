package com.prontaentrega.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.prontaentrega.models.exception.CampoFaltanteException;
import com.prontaentrega.models.exception.ValorNegativoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios de dominio para las invariantes de Jugador.
 */
class JugadorTest {
    @Test
    void actualizarEstadisticasShouldReplaceSportValues() {
        BolsaDeJugador jugador = BolsaDeJugador.desdeEstadisticas(
                101,
                "Ana Gomez",
                "Rojo FC",
                Liga.PREMIER_LEAGUE,
                20,
                170,
                60,
                Posicion.FORWARD,
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
                Posicion.MIDFIELDER,
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

        Jugador datosJugador = jugador.getEstadisticasDeportivas().getJugador();
        EstadisticasDeportivas estadisticas = jugador.getEstadisticasDeportivas();
        assertEquals(21, datosJugador.getEdad());
        assertEquals(171, datosJugador.getAltura());
        assertEquals(61, datosJugador.getPeso());
        assertEquals(Posicion.MIDFIELDER, datosJugador.getPosicion());
        assertFalse(estadisticas.getActivo());
        assertEquals(3, estadisticas.getGoles());
        assertEquals(4, estadisticas.getAsistencias());
        assertEquals(new BigDecimal("2.5"), estadisticas.getDisparosPorPartido());
        assertEquals(new BigDecimal("2.0"), estadisticas.getKeyPassesPorPartido());
        assertEquals(new BigDecimal("3.0"), estadisticas.getDribblesGanadosPorPartido());
        assertEquals(new BigDecimal("1.0"), estadisticas.getFaltasCometidasPorPartido());
        assertEquals(new BigDecimal("7.8"), estadisticas.getRating());
        assertEquals(700, estadisticas.getMinutosJugados());

        // Estos valores no deberían modificarse al actualizar estadísticas.
        assertEquals(new BigDecimal("1"), jugador.getCotizacionActual());
        assertEquals(100, jugador.getTokensRestantes());
    }

    @Test
    void desdeEstadisticasShouldRejectBlankIdentity() {
        assertThrows(
                CampoFaltanteException.class,
                () -> BolsaDeJugador.desdeEstadisticas(
                        101,
                        " ",
                        "Rojo FC", Liga.PREMIER_LEAGUE, 20,
                        170, 60, Posicion.FORWARD, true, 1,
                        2, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                        500, LocalDateTime.now()
                )
        );
    }

    @Test
    void desdeEstadisticasShouldRejectNegativeGoalsStats() {
        assertThrows(
                ValorNegativoException.class,
                () -> BolsaDeJugador.desdeEstadisticas(
                        101, "Ana Gomez", "Rojo FC", Liga.PREMIER_LEAGUE, 20,
                        170, 60, Posicion.FORWARD, true,
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
        BolsaDeJugador jugador = BolsaDeJugador.desdeEstadisticas(101, "Ana Gomez",
                "Rojo FC", Liga.PREMIER_LEAGUE, 20, 170, 60, Posicion.FORWARD,
                true, 1, 2, new BigDecimal("1.5"),
                new BigDecimal("1.0"), new BigDecimal("2.0"), new BigDecimal("0.5"),
                new BigDecimal("7.1"), 500, LocalDateTime.now()
        );

        jugador.actualizarEstadisticas(21, 171, 61, Posicion.MIDFIELDER, false,
                10, 8, new BigDecimal("3.5"), new BigDecimal("2.5"),
                new BigDecimal("4.0"), new BigDecimal("1.5"), new BigDecimal("8.9"),
                1500, LocalDateTime.now(), "WhoScored"
        );

        Jugador datosJugador = jugador.getEstadisticasDeportivas().getJugador();
        EstadisticasDeportivas estadisticas = jugador.getEstadisticasDeportivas();
        assertEquals(21, datosJugador.getEdad());
        assertEquals(171, datosJugador.getAltura());
        assertEquals(61, datosJugador.getPeso());
        assertEquals(Posicion.MIDFIELDER, datosJugador.getPosicion());
        assertFalse(estadisticas.getActivo());

        assertEquals(10, estadisticas.getGoles());
        assertEquals(8, estadisticas.getAsistencias());
        assertEquals(new BigDecimal("3.5"), estadisticas.getDisparosPorPartido());
        assertEquals(new BigDecimal("2.5"), estadisticas.getKeyPassesPorPartido());
        assertEquals(new BigDecimal("4.0"), estadisticas.getDribblesGanadosPorPartido());
        assertEquals(new BigDecimal("1.5"), estadisticas.getFaltasCometidasPorPartido());
        assertEquals(new BigDecimal("8.9"), estadisticas.getRating());
        assertEquals(1500, estadisticas.getMinutosJugados());
    }

    @Test
    void actualizarEstadisticasShouldPreserveMarketValues() {
        BolsaDeJugador jugador = BolsaDeJugador.desdeEstadisticas(
                101, "Ana Gomez", "Rojo FC", Liga.PREMIER_LEAGUE, 20, 170,
                60, Posicion.FORWARD, true, 1, 2, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, 500, LocalDateTime.now()
        );

        jugador.actualizarEstadisticas(
                21, 171, 61, Posicion.MIDFIELDER, false, 10, 8,
                new BigDecimal("3.5"), new BigDecimal("2.5"), new BigDecimal("4.0"), new BigDecimal("1.5"),
                new BigDecimal("8.9"), 1500, LocalDateTime.now(), "WhoScored"
        );

        assertEquals(new BigDecimal("1"), jugador.getCotizacionActual());
        assertEquals(100, jugador.getTokensRestantes());
    }

}
