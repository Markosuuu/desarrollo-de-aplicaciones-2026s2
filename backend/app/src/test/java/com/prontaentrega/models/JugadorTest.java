package com.prontaentrega.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.prontaentrega.models.exception.CampoFaltanteException;
import com.prontaentrega.models.exception.ValorNegativoException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios de dominio para las invariantes de Jugador.
 */
class JugadorTest {
    @Test
    void actualizarEstadisticasShouldReplaceSportValues() {
        EstadisticasDeportivas estadisticas = getEstadisticas();
        BolsaDeJugador bolsaJugador = BolsaDeJugador.desdeEstadisticas(estadisticas, LocalDateTime.now());

        bolsaJugador.getEstadisticasDeportivas().getJugador().actualizar(
                21,
                171,
                61,
                Posicion.MIDFIELDER
        );

        bolsaJugador.getEstadisticasDeportivas().actualizar(
                bolsaJugador.getEstadisticasDeportivas().getJugador(),
                false,
                3,
                4,
                new BigDecimal("2.5"),
                new BigDecimal("2.0"),
                new BigDecimal("3.0"),
                new BigDecimal("1.0"),
                new BigDecimal("7.8"),
                700
        );

        bolsaJugador.actualizarEstadisticas(
                bolsaJugador.getEstadisticasDeportivas(),
                LocalDateTime.now(),
                "WhoScored"
        );

        Jugador datosJugador = bolsaJugador.getEstadisticasDeportivas().getJugador();
        EstadisticasDeportivas estadisticasActualizadas = bolsaJugador.getEstadisticasDeportivas();
        assertEquals(21, datosJugador.getEdad());
        assertEquals(171, datosJugador.getAltura());
        assertEquals(61, datosJugador.getPeso());
        assertEquals(Posicion.MIDFIELDER, datosJugador.getPosicion());
        assertFalse(estadisticasActualizadas.getActivo());
        assertEquals(3, estadisticasActualizadas.getGoles());
        assertEquals(4, estadisticasActualizadas.getAsistencias());
        assertEquals(new BigDecimal("2.5"), estadisticasActualizadas.getDisparosPorPartido());
        assertEquals(new BigDecimal("2.0"), estadisticasActualizadas.getKeyPassesPorPartido());
        assertEquals(new BigDecimal("3.0"), estadisticasActualizadas.getDribblesGanadosPorPartido());
        assertEquals(new BigDecimal("1.0"), estadisticasActualizadas.getFaltasCometidasPorPartido());
        assertEquals(new BigDecimal("7.8"), estadisticasActualizadas.getRating());
        assertEquals(700, estadisticasActualizadas.getMinutosJugados());

        // Estos valores no deberían modificarse al actualizar estadísticas.
        assertEquals(new BigDecimal("1"), bolsaJugador.getCotizacionActual());
        assertEquals(100, bolsaJugador.getTokensRestantes());
    }

    private static @NotNull EstadisticasDeportivas getEstadisticas() {
        Jugador jugador = new Jugador(
                101,
                "Ana Gomez",
                "Rojo FC",
                Liga.PREMIER_LEAGUE,
                Posicion.FORWARD,
                24,
                170,
                60
        );

        return new EstadisticasDeportivas(
                jugador,
                true,
                1,
                1,
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                100
        );
    }

    @Test
    void actualizarEstadisticasShouldReplaceAllStatValues() {
        EstadisticasDeportivas estadisticas = getEstadisticas();

        BolsaDeJugador bolsaJugador = BolsaDeJugador.desdeEstadisticas(estadisticas, LocalDateTime.now());

        bolsaJugador.getEstadisticasDeportivas().getJugador().actualizar(
                21,
                171,
                61,
                Posicion.MIDFIELDER
        );

        bolsaJugador.getEstadisticasDeportivas().actualizar(
                bolsaJugador.getEstadisticasDeportivas().getJugador(),
                false,
                10,
                8,
                new BigDecimal("3.5"),
                new BigDecimal("2.5"),
                new BigDecimal("4.0"),
                new BigDecimal("1.5"),
                new BigDecimal("8.9"),
                1500
        );

        bolsaJugador.actualizarEstadisticas(
                bolsaJugador.getEstadisticasDeportivas(),
                LocalDateTime.now(),
                "WhoScored"
        );

        Jugador datosJugador = bolsaJugador.getEstadisticasDeportivas().getJugador();
        EstadisticasDeportivas estadisticasActualizadas = bolsaJugador.getEstadisticasDeportivas();
        assertEquals(21, datosJugador.getEdad());
        assertEquals(171, datosJugador.getAltura());
        assertEquals(61, datosJugador.getPeso());
        assertEquals(Posicion.MIDFIELDER, datosJugador.getPosicion());
        assertFalse(estadisticasActualizadas.getActivo());

        assertEquals(10, estadisticasActualizadas.getGoles());
        assertEquals(8, estadisticasActualizadas.getAsistencias());
        assertEquals(new BigDecimal("3.5"), estadisticasActualizadas.getDisparosPorPartido());
        assertEquals(new BigDecimal("2.5"), estadisticasActualizadas.getKeyPassesPorPartido());
        assertEquals(new BigDecimal("4.0"), estadisticasActualizadas.getDribblesGanadosPorPartido());
        assertEquals(new BigDecimal("1.5"), estadisticasActualizadas.getFaltasCometidasPorPartido());
        assertEquals(new BigDecimal("8.9"), estadisticasActualizadas.getRating());
        assertEquals(1500, estadisticasActualizadas.getMinutosJugados());
    }

    @Test
    void actualizarEstadisticasShouldPreserveMarketValues() {
        EstadisticasDeportivas estadisticas = getEstadisticas();

        BolsaDeJugador bolsaJugador = BolsaDeJugador.desdeEstadisticas(estadisticas, LocalDateTime.now());

        bolsaJugador.getEstadisticasDeportivas().getJugador().actualizar(
                21,
                171,
                61,
                Posicion.MIDFIELDER
        );

        bolsaJugador.getEstadisticasDeportivas().actualizar(
                bolsaJugador.getEstadisticasDeportivas().getJugador(),
                false,
                10,
                8,
                new BigDecimal("3.5"),
                new BigDecimal("2.5"),
                new BigDecimal("4.0"),
                new BigDecimal("1.5"),
                new BigDecimal("8.9"),
                1500
        );

        bolsaJugador.actualizarEstadisticas(
                bolsaJugador.getEstadisticasDeportivas(),
                LocalDateTime.now(),
                "WhoScored"
        );

        assertEquals(new BigDecimal("1"), bolsaJugador.getCotizacionActual());
        assertEquals(100, bolsaJugador.getTokensRestantes());
    }

}
