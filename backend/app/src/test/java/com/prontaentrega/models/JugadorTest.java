package com.prontaentrega.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.prontaentrega.models.exception.CampoFaltanteException;
import com.prontaentrega.models.exception.LigaNoSoportadaException;
import com.prontaentrega.models.exception.PosicionNoExistenteException;
import com.prontaentrega.models.exception.ValorNegativoException;
import com.prontaentrega.models.jugadores.builders.EstadisticasDeportivasDatos;
import com.prontaentrega.models.jugadores.Liga;
import com.prontaentrega.models.jugadores.Posicion;
import com.prontaentrega.models.jugadores.BolsaDeJugador;
import com.prontaentrega.models.jugadores.EstadisticasDeportivas;
import com.prontaentrega.models.jugadores.Jugador;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("modelo")
class JugadorTest {
    private Jugador jugador;
    private EstadisticasDeportivas estadisticas;
    private BolsaDeJugador bolsa;

    @BeforeEach
    void setUp(){
        jugador = new Jugador(1, "Messi", "Barcelona FC", Liga.LALIGA, Posicion.FORWARD,
                40, 170, 70);

        estadisticas = new EstadisticasDeportivas(jugador,
                true, 10, 10,
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE, 120);

        bolsa = new BolsaDeJugador(estadisticas, BigDecimal.ONE, LocalDateTime.now(), "Fuente", 100);
    }

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

        EstadisticasDeportivasDatos datos = EstadisticasDeportivasDatos.builder()
                .jugador(bolsaJugador.getEstadisticasDeportivas().getJugador())
                .activo(false)
                .goles(3)
                .asistencias(4)
                .disparosPorPartido(new BigDecimal("2.5"))
                .keyPassesPorPartido(new BigDecimal("2.0"))
                .dribblesGanadosPorPartido(new BigDecimal("3.0"))
                .faltasCometidasPorPartido(new BigDecimal("1.0"))
                .rating(new BigDecimal("7.8"))
                .minutosJugados(700)
                .build();

        bolsaJugador.getEstadisticasDeportivas().actualizar(datos);

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

        EstadisticasDeportivasDatos datos = EstadisticasDeportivasDatos.builder()
                .jugador(bolsaJugador.getEstadisticasDeportivas().getJugador())
                .activo(false)
                .goles(10)
                .asistencias(8)
                .disparosPorPartido(new BigDecimal("3.5"))
                .keyPassesPorPartido(new BigDecimal("2.5"))
                .dribblesGanadosPorPartido(new BigDecimal("4.0"))
                .faltasCometidasPorPartido(new BigDecimal("1.5"))
                .rating(new BigDecimal("8.9"))
                .minutosJugados(1500)
                .build();

        bolsaJugador.getEstadisticasDeportivas().actualizar(datos);

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

        EstadisticasDeportivasDatos datos = EstadisticasDeportivasDatos.builder()
                .activo(false)
                .goles(10)
                .asistencias(8)
                .disparosPorPartido(new BigDecimal("3.5"))
                .keyPassesPorPartido(new BigDecimal("2.5"))
                .dribblesGanadosPorPartido(new BigDecimal("4.0"))
                .faltasCometidasPorPartido(new BigDecimal("1.5"))
                .rating(new BigDecimal("8.9"))
                .minutosJugados(1500)
                .build();

        bolsaJugador.getEstadisticasDeportivas().actualizar(datos);

        bolsaJugador.actualizarEstadisticas(
                bolsaJugador.getEstadisticasDeportivas(),
                LocalDateTime.now(),
                "WhoScored"
        );

        assertEquals(new BigDecimal("1"), bolsaJugador.getCotizacionActual());
        assertEquals(100, bolsaJugador.getTokensRestantes());
    }

    @Test
    void noSeCreaUnJugadorSiNoVieneDeWhoScored() {
        CampoFaltanteException ex = assertThrows(CampoFaltanteException.class,
                () -> new Jugador(null, "Messi", "Barcelona FC", Liga.LALIGA, Posicion.FORWARD,
                        40, 170, 70));

        assertEquals("El ID de WhoScored es obligatorio", ex.getMessage());
    }

    @Test
    void noSeCreaUnJugadorSiNoTieneLiga() {
        CampoFaltanteException ex = assertThrows(CampoFaltanteException.class,
                () -> new Jugador(1, "Messi", "Barcelona FC", null, Posicion.FORWARD,
                        40, 170, 70));

        assertEquals("La liga es obligatoria", ex.getMessage());
    }

    @Test
    void noSeCreaUnJugadorSiNoTieneNombre() {
        CampoFaltanteException ex = assertThrows(CampoFaltanteException.class,
                () -> new Jugador(1, null, "Barcelona FC", Liga.LALIGA, Posicion.FORWARD,
                        40, 170, 70));

        assertEquals("El nombre es obligatorio", ex.getMessage());
    }

    @Test
    void noSeCreaUnJugadorSiNoTieneEquipo() {
        CampoFaltanteException ex = assertThrows(CampoFaltanteException.class,
                () -> new Jugador(1, "Messi", null, Liga.LALIGA, Posicion.FORWARD,
                        40, 170, 70));

        assertEquals("El equipo es obligatorio", ex.getMessage());
    }

    @Test
    void noSeCreaUnJugadorSiLaEdadEsNegativa() {
        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> new Jugador(1, "Messi", "Barcelona FC", Liga.LALIGA, Posicion.FORWARD,
                        -40, 170, 70));

        assertEquals("La edad no puede ser negativa", ex.getMessage());
    }

    @Test
    void noSeCreaUnJugadorSiLaAlturaEsNegativa() {
        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> new Jugador(1, "Messi", "Barcelona FC", Liga.LALIGA, Posicion.FORWARD,
                        40, -170, 70));

        assertEquals("La altura no puede ser negativa", ex.getMessage());
    }

    @Test
    void noSeCreaUnJugadorSiElPesoEsNegativo() {
        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> new Jugador(1, "Messi", "Barcelona FC", Liga.LALIGA, Posicion.FORWARD,
                        40, 170, -70));

        assertEquals("El peso no puede ser negativo", ex.getMessage());
    }

    @Test
    void noSeCreaUnJugadorSiEl_EsNulo() {
        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> new Jugador(1, "Messi", "Barcelona FC", Liga.LALIGA, Posicion.FORWARD,
                        null, 170, 70));

        assertEquals("La edad no puede ser negativa", ex.getMessage());
    }

    @Test
    void noSeCreaUnJugadorSiLaPosicionNoExiste() {
        LigaNoSoportadaException ex = assertThrows(LigaNoSoportadaException.class,
                () -> new Jugador(1, "Messi", "Barcelona FC", Liga.fromValue("MLS"), Posicion.FORWARD,
                        40, 170, 70));

        assertEquals("Liga no soportada: MLS", ex.getMessage());
    }

    @Test
    void noSeCreaUnJugadorSiLaLigaNoExiste() {
        PosicionNoExistenteException ex = assertThrows(PosicionNoExistenteException.class,
                () -> new Jugador(1, "Messi", "Barcelona FC", Liga.LALIGA, Posicion.fromValue("atajador"),
                        40, 170, 70));

        assertEquals("Posicion no soportada: atajador", ex.getMessage());
    }

    @Test
    void actualizarUnJugadorConValoresNulos_NoActualizaSusCampos() {
        jugador.actualizar(null, null, null, null);

        assertEquals(40, jugador.getEdad());
        assertEquals(170, jugador.getAltura());
        assertEquals(70, jugador.getPeso());
        assertEquals(Posicion.FORWARD, jugador.getPosicion());
    }

    /* Tests de estadistícas deportivas */

    @Test
    void noSeCreanEstadisticasSiLosGolesSonNegativos() {
        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> new EstadisticasDeportivas(jugador,
                        true, -10, 10,
                        BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                        BigDecimal.ONE, BigDecimal.ONE, 120));

        assertEquals("Los goles no pueden ser negativos", ex.getMessage());
    }

    @Test
    void noSeCreanEstadisticasSiLasAsistenciasSonNegativas() {
        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> new EstadisticasDeportivas(jugador,
                        true, 10, -10,
                        BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                        BigDecimal.ONE, BigDecimal.ONE, 120));

        assertEquals("Las asistencias no pueden ser negativas", ex.getMessage());
    }

    @Test
    void noSeCreanEstadisticasSiLosDisparosPorPartidoSonNegativos() {
        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> new EstadisticasDeportivas(jugador,
                        true, 10, 10,
                        BigDecimal.valueOf(-1), BigDecimal.ONE, BigDecimal.ONE,
                        BigDecimal.ONE, BigDecimal.ONE, 120));

        assertEquals("Los disparos por partido no pueden ser negativos", ex.getMessage());
    }

    @Test
    void noSeCreanEstadisticasSiLosPasesSonNegativos() {
        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> new EstadisticasDeportivas(jugador,
                        true, 10, 10,
                        BigDecimal.ONE, BigDecimal.valueOf(-1), BigDecimal.ONE,
                        BigDecimal.ONE, BigDecimal.ONE, 120));

        assertEquals("Los key passes por partido no pueden ser negativos", ex.getMessage());
    }

    @Test
    void noSeCreanEstadisticasSiLosDribblesGanadosSonNegativos() {

        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> new EstadisticasDeportivas(jugador,
                        true, 10, 10,
                        BigDecimal.ONE, BigDecimal.ONE, BigDecimal.valueOf(-1),
                        BigDecimal.ONE, BigDecimal.ONE, 120));

        assertEquals("Los dribbles ganados por partido no pueden ser negativos", ex.getMessage());
    }

    @Test
    void noSeCreanEstadisticasSiLasFaltasSonNegativos() {
        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> new EstadisticasDeportivas(jugador,
                        true, 10, 10,
                        BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                        BigDecimal.valueOf(-1), BigDecimal.ONE, 120));

        assertEquals("Las faltas cometidas por partido no pueden ser negativas", ex.getMessage());
    }

    @Test
    void noSeCreanEstadisticasSiElRatingEsNegativo() {

        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> new EstadisticasDeportivas(jugador,
                        true, 10, 10,
                        BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                        BigDecimal.ONE, BigDecimal.valueOf(-1), 120));

        assertEquals("El rating no puede ser negativo", ex.getMessage());
    }

    @Test
    void noSeCreanEstadisticasSiLosMinutosSonNegativos() {
        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> new EstadisticasDeportivas(jugador,
                        true, 10, 10,
                        BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                        BigDecimal.ONE, BigDecimal.ONE, -120));

        assertEquals("Los minutos jugados no pueden ser negativos", ex.getMessage());
    }

    @Test
    void noSeCreanEstadisticasSiLosGolesSonNulos() {
        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> new EstadisticasDeportivas(jugador,
                        true, null, 10,
                        BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                        BigDecimal.ONE, BigDecimal.ONE, 120));

        assertEquals("Los goles no pueden ser negativos", ex.getMessage());
    }

    @Test
    void noSeCreanEstadisticasSiElRatingEsNulo() {
        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> new EstadisticasDeportivas(jugador,
                        true, 10, 10,
                        BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                        BigDecimal.ONE, null, 120));

        assertEquals("El rating no puede ser negativo", ex.getMessage());
    }

    // Tests de BolsaDeJugador

    @Test
    void noSeCreaUnaBolsaSiNoTieneEstadisticasDeportivas() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new BolsaDeJugador(null, BigDecimal.ONE, LocalDateTime.now(), "WhoScored", 100));

        assertEquals("estadisticasDeportivas is marked non-null but is null", ex.getMessage());
    }

    @Test
    void noSeCreaUnaBolsaSiNoTieneCotizacionActual() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new BolsaDeJugador(estadisticas, null, LocalDateTime.now(), "WhoScored", 100));

        assertEquals("cotizacionActual is marked non-null but is null", ex.getMessage());
    }

    @Test
    void noSeCreaUnaBolsaSiNoTieneFechaDeActualizacion() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new BolsaDeJugador(estadisticas, BigDecimal.ONE, null, "WhoScored", 100));

        assertEquals("fechaActualizacion is marked non-null but is null", ex.getMessage());
    }

    @Test
    void noSeCreaUnaBolsaSiNoTieneFuente() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new BolsaDeJugador(estadisticas, BigDecimal.ONE, LocalDateTime.now(), null, 100));

        assertEquals("fuente is marked non-null but is null", ex.getMessage());
    }

    @Test
    void noSeCreaUnaBolsaSiNoTieneTokensRestantes() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new BolsaDeJugador(estadisticas, BigDecimal.ONE, LocalDateTime.now(), "WhoScored", null));

        assertEquals("tokensRestantes is marked non-null but is null", ex.getMessage());
    }

    @Test
    void noSeCreaUnaBolsaDesdeEstadisticasSiLasEstadisticasSonNulas() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> BolsaDeJugador.desdeEstadisticas(null, LocalDateTime.now()));

        assertEquals("estadisticas is marked non-null but is null", ex.getMessage());
    }

    @Test
    void noSeCreaUnaBolsaDesdeEstadisticasSiLaFechaEsNula() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> BolsaDeJugador.desdeEstadisticas(estadisticas, null));

        assertEquals("fecha is marked non-null but is null", ex.getMessage());
    }

    @Test
    void noSeActualizanLasEstadisticasSiLasNuevasEstadisticasSonNulas() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> bolsa.actualizarEstadisticas(null, LocalDateTime.now(), "WhoScored"));

        assertEquals("estadisticas is marked non-null but is null", ex.getMessage());
    }

    @Test
    void noSeActualizanLasEstadisticasSiLaFechaEsNula() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> bolsa.actualizarEstadisticas(estadisticas, null, "WhoScored"));

        assertEquals("fecha is marked non-null but is null", ex.getMessage());
    }

    @Test
    void noSeActualizanLasEstadisticasSiLaFuenteEsNula() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> bolsa.actualizarEstadisticas(estadisticas, LocalDateTime.now(), null));

        assertEquals("fuente is marked non-null but is null", ex.getMessage());
    }

    // Posicion

    @Test
    void fromValue_deberiaDevolverNullSiElValorEsNulo() {
        assertNull(Posicion.fromValue(null));
    }

    @Test
    void fromValue_deberiaDevolverNullSiElValorEstaVacio() {
        assertNull(Posicion.fromValue(""));
    }

    @Test
    void fromValue_deberiaDevolverNullSiElValorSoloTieneEspacios() {
        assertNull(Posicion.fromValue("   "));
    }

    // Liga

    @Test
    void fromValue_deberiaLanzarCampoFaltanteSiElValorEsNulo() {
        CampoFaltanteException ex = assertThrows(CampoFaltanteException.class,
                () -> Liga.fromValue(null));

        assertEquals("La liga es obligatoria", ex.getMessage());
    }

}
