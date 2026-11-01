package com.prontaentrega.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.prontaentrega.models.DatosValoracion;
import com.prontaentrega.persistence.repository.CotizacionRepository;
import com.prontaentrega.persistence.repository.CotizacionRepository.ActualizacionCotizacion;
import com.prontaentrega.utils.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/** Comprueba lectura keyset y escritura batch con PostgreSQL real. */
@SpringBootTest
@Tag("integracion")
class QuoteRecalculationRepositoryTest extends AbstractIntegrationTest {
    @Autowired
    private CotizacionRepository quoteRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    @AfterEach
    void limpiarDatosDeJugadores() {
        jdbcTemplate.update("DELETE FROM bolsas_de_jugadores");
        jdbcTemplate.update("DELETE FROM estadisticas_deportivas");
        jdbcTemplate.update("DELETE FROM jugadores");
    }

    /** Lee dos registros en páginas acotadas y persiste sus cotizaciones calculadas. */
    @Test
    void paginaProyeccionesYActualizaCotizacionesEnLotes() {
        UUID primeraBolsa = insertarJugador(1, new BigDecimal("10.00"), new BigDecimal("1.2"));
        UUID segundaBolsa = insertarJugador(2, new BigDecimal("20.00"), null);

        List<DatosValoracion> primeraPagina = quoteRepository.buscarPagina(null, 1);
        assertThat(primeraPagina).hasSize(1);
        List<DatosValoracion> segundaPagina = quoteRepository.buscarPagina(
                primeraPagina.getFirst().bolsaId(), 1);
        assertThat(segundaPagina).hasSize(1);
        assertThat(primeraPagina.getFirst().keyPassesPorPartido() == null
                || segundaPagina.getFirst().keyPassesPorPartido() == null).isTrue();
        List<DatosValoracion> paginaComun = quoteRepository.buscarPagina(null, 10);
        assertThat(paginaComun).hasSize(2);
        assertThat(paginaComun).extracting(DatosValoracion::keyPassesPorPartido)
                .contains(new BigDecimal("1.20"));
        assertThat(paginaComun.stream().anyMatch(datos -> datos.keyPassesPorPartido() == null))
                .isTrue();

        int actualizados = quoteRepository.actualizarCotizaciones(List.of(
                new ActualizacionCotizacion(primeraBolsa, new BigDecimal("15.00")),
                new ActualizacionCotizacion(segundaBolsa, new BigDecimal("25.00"))));

        assertThat(actualizados).isEqualTo(2);
        assertThat(quoteVigente(primeraBolsa)).isEqualByComparingTo("15.00");
        assertThat(quoteVigente(segundaBolsa)).isEqualByComparingTo("25.00");
    }

        /** Inserta un jugador relacional de prueba y devuelve el ID de su bolsa. */
        private UUID insertarJugador(int sufijo, BigDecimal cotizacion, BigDecimal pasesClave) {
        UUID jugadorId = UUID.randomUUID();
        UUID estadisticasId = UUID.randomUUID();
        UUID bolsaId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO jugadores (id, whoscored_id, nombre, equipo, liga, edad)
                VALUES (?, ?, ?, ?, ?, ?)
                """, jugadorId, 900000 + sufijo, "Jugador " + sufijo, "Equipo", "LaLiga", 25);
        jdbcTemplate.update("""
                INSERT INTO estadisticas_deportivas
                    (id, jugador_id, activo, goles, asistencias, disparos_por_partido,
                     key_passes_por_partido, dribbles_ganados_por_partido,
                     faltas_cometidas_por_partido, rating)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, estadisticasId, jugadorId, true, 2, 1, new BigDecimal("1.1"),
                pasesClave, new BigDecimal("0.4"), new BigDecimal("0.2"), new BigDecimal("6.5"));
        jdbcTemplate.update("""
                INSERT INTO bolsas_de_jugadores
                    (id, estadisticas_deportivas_id, cotizacion_actual, fecha_actualizacion,
                     fuente, tokens_restantes)
                VALUES (?, ?, ?, ?, ?, ?)
                """, bolsaId, estadisticasId, cotizacion,
                LocalDateTime.of(2026, 1, 1, 0, 0), "test", 0);
        return bolsaId;
    }

        /** Lee la cotización almacenada para comprobar el resultado del batch update. */
        private BigDecimal quoteVigente(UUID bolsaId) {
        return jdbcTemplate.queryForObject(
                "SELECT cotizacion_actual FROM bolsas_de_jugadores WHERE id = ?",
                BigDecimal.class, bolsaId);
    }
}