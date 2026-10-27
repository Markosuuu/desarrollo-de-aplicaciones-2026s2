package com.prontaentrega.services;

import static org.assertj.core.api.Assertions.assertThat;

import com.prontaentrega.models.estrategias.ConfiguracionEstrategiaPonderada;
import com.prontaentrega.models.EstadoEstrategiaCotizacion;
import com.prontaentrega.models.exception.CotizacionInvalidaException;
import com.prontaentrega.persistence.repository.CotizacionRepository;
import com.prontaentrega.schedulers.CotizacionScheduler;
import com.prontaentrega.services.dto.ResultadoRecotizacion;
import com.prontaentrega.utils.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/** Verifica cálculo y omisiones de la operación completa contra PostgreSQL. */
@SpringBootTest
@Tag("integracion")
class QuoteRecalculationServiceIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private CotizacionService administrationService;

    @Autowired
    private CotizacionRepository strategyRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

        @Autowired
        private CotizacionScheduler scheduler;

    /** Actualiza jugadores válidos y omite aquellos sin todas las métricas ponderadas. */
    @Test
    void recalculaElegiblesYOmiteMetricasAusentesEnPostgres() {
        ConfiguracionEstrategiaPonderada config = new ConfiguracionEstrategiaPonderada(
                BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("2"));
        strategyRepository.guardarConfiguracion("ponderada", config);
        strategyRepository.guardarEstado(new EstadoEstrategiaCotizacion("ponderada"));
        UUID jugadorValido = UUID.randomUUID();
        UUID jugadorOmitido = UUID.randomUUID();
        UUID bolsaValida = insertarJugador(jugadorValido, 910001, "10.00", "1.0");
        UUID bolsaOmitida = insertarJugador(jugadorOmitido, 910002, "20.00", null);

        try {
            ResultadoRecotizacion result = administrationService.recalcularEstrategiaActiva();

            assertThat(result).isEqualTo(new ResultadoRecotizacion("ponderada", 1, 1));
            assertThat(cotizacion(bolsaValida)).isEqualByComparingTo("16.00");
            assertThat(cotizacion(bolsaOmitida)).isEqualByComparingTo("20.00");
        } finally {
            strategyRepository.guardarEstado(new EstadoEstrategiaCotizacion(null));
            jdbcTemplate.update("DELETE FROM bolsas_de_jugadores WHERE id IN (?, ?)",
                    bolsaValida, bolsaOmitida);
            jdbcTemplate.update("DELETE FROM estadisticas_deportivas WHERE jugador_id IN (?, ?)",
                    jugadorValido, jugadorOmitido);
            jdbcTemplate.update("DELETE FROM jugadores WHERE id IN (?, ?)",
                    jugadorValido, jugadorOmitido);
            jdbcTemplate.update("DELETE FROM configuracion_estrategia_ponderada WHERE estrategia_id = ?",
                    "ponderada");
        }
    }

    /** Confirma que el scheduler reutiliza la operación manual sobre el mismo estado inicial. */
    @Test
    void ejecucionManualYSemanalProducenMismasCotizaciones() {
        ConfiguracionEstrategiaPonderada configuracion = new ConfiguracionEstrategiaPonderada(
                BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("2"));
        strategyRepository.guardarConfiguracion("ponderada", configuracion);
        strategyRepository.guardarEstado(new EstadoEstrategiaCotizacion("ponderada"));
        UUID jugadorValido = UUID.randomUUID();
        UUID jugadorOmitido = UUID.randomUUID();
        UUID bolsaValida = insertarJugador(jugadorValido, 910011, "10.00", "1.0");
        UUID bolsaOmitida = insertarJugador(jugadorOmitido, 910012, "20.00", null);

        try {
            ResultadoRecotizacion resultadoManual = administrationService.recalcularEstrategiaActiva();
            BigDecimal cotizacionManual = cotizacion(bolsaValida);
            jdbcTemplate.update("UPDATE bolsas_de_jugadores SET cotizacion_actual = ? WHERE id = ?",
                    new BigDecimal("10.00"), bolsaValida);

            scheduler.recalcularCotizacionesSemanalmente();

            assertThat(cotizacion(bolsaValida)).isEqualByComparingTo(cotizacionManual);
            assertThat(cotizacion(bolsaOmitida)).isEqualByComparingTo("20.00");
            assertThat(resultadoManual.processedPlayers()).isEqualTo(1);
            assertThat(resultadoManual.skippedPlayers()).isEqualTo(1);
        } finally {
            strategyRepository.guardarEstado(new EstadoEstrategiaCotizacion(null));
            jdbcTemplate.update("DELETE FROM bolsas_de_jugadores WHERE id IN (?, ?)",
                    bolsaValida, bolsaOmitida);
            jdbcTemplate.update("DELETE FROM estadisticas_deportivas WHERE jugador_id IN (?, ?)",
                    jugadorValido, jugadorOmitido);
            jdbcTemplate.update("DELETE FROM jugadores WHERE id IN (?, ?)",
                    jugadorValido, jugadorOmitido);
            jdbcTemplate.update("DELETE FROM configuracion_estrategia_ponderada WHERE estrategia_id = ?",
                    "ponderada");
        }
    }

    /** Confirma que la operación manual y la programada producen el mismo resultado. */
    @Test
    void ejecucionManualYSchedulerProducenResultadosEquivalentes() {
        ConfiguracionEstrategiaPonderada config = new ConfiguracionEstrategiaPonderada(
                BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("2"));
        strategyRepository.guardarConfiguracion("ponderada", config);
        strategyRepository.guardarEstado(new EstadoEstrategiaCotizacion("ponderada"));
        UUID jugadorValido = UUID.randomUUID();
        UUID jugadorOmitido = UUID.randomUUID();
        UUID bolsaValida = insertarJugador(jugadorValido, 910011, "10.00", "1.0");
        UUID bolsaOmitida = insertarJugador(jugadorOmitido, 910012, "20.00", null);

        try {
            ResultadoRecotizacion manual = administrationService.recalcularEstrategiaActiva();
            BigDecimal cotizacionManual = cotizacion(bolsaValida);
            jdbcTemplate.update("UPDATE bolsas_de_jugadores SET cotizacion_actual = ? WHERE id = ?",
                    new BigDecimal("10.00"), bolsaValida);

            scheduler.recalcularCotizacionesSemanalmente();

            assertThat(cotizacion(bolsaValida)).isEqualByComparingTo(cotizacionManual);
            assertThat(cotizacion(bolsaOmitida)).isEqualByComparingTo("20.00");
            assertThat(manual.processedPlayers()).isEqualTo(1);
            assertThat(manual.skippedPlayers()).isEqualTo(1);
        } finally {
            strategyRepository.guardarEstado(new EstadoEstrategiaCotizacion(null));
            jdbcTemplate.update("DELETE FROM bolsas_de_jugadores WHERE id IN (?, ?)",
                    bolsaValida, bolsaOmitida);
            jdbcTemplate.update("DELETE FROM estadisticas_deportivas WHERE jugador_id IN (?, ?)",
                    jugadorValido, jugadorOmitido);
            jdbcTemplate.update("DELETE FROM jugadores WHERE id IN (?, ?)",
                    jugadorValido, jugadorOmitido);
            jdbcTemplate.update("DELETE FROM configuracion_estrategia_ponderada WHERE estrategia_id = ?",
                    "ponderada");
        }
    }

    /** Revierte el primer lote cuando una cotización inválida aparece en la página siguiente. */
    @Test
    void revierteLotesPreviosSiUnaCotizacionTardiaEsInvalida() {
        ConfiguracionEstrategiaPonderada config = new ConfiguracionEstrategiaPonderada(
                BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("2"));
        strategyRepository.guardarConfiguracion("ponderada", config);
        strategyRepository.guardarEstado(new EstadoEstrategiaCotizacion("ponderada"));
        insertarMercadoNumerado();

        try {
            org.assertj.core.api.Assertions.assertThatThrownBy(
                    () -> administrationService.recalcularEstrategiaActiva())
                    .isInstanceOf(CotizacionInvalidaException.class);
            assertThat(cotizacion(new UUID(2, 1))).isEqualByComparingTo("10.00");
            assertThat(cotizacion(new UUID(2, 201))).isEqualByComparingTo("1.00");
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT estrategia_activa_id FROM estado_estrategia_cotizacion WHERE id = ?",
                    String.class, "global")).isEqualTo("ponderada");
        } finally {
            strategyRepository.guardarEstado(new EstadoEstrategiaCotizacion(null));
            borrarMercadoNumerado();
            jdbcTemplate.update("DELETE FROM configuracion_estrategia_ponderada WHERE estrategia_id = ?",
                    "ponderada");
        }
    }

        /** Inserta un jugador y las tablas estadísticas/bolsa que requiere el join de valoración. */
        private UUID insertarJugador(UUID jugadorId, int whoscoredId, String cotizacion,
                                 String pasesClave) {
        UUID estadisticasId = UUID.randomUUID();
        UUID bolsaId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO jugadores (id, whoscored_id, nombre, equipo, liga, edad)
                VALUES (?, ?, ?, ?, ?, ?)
                """, jugadorId, whoscoredId, "Jugador " + whoscoredId, "Equipo", "LaLiga", 25);
        jdbcTemplate.update("""
                INSERT INTO estadisticas_deportivas
                    (id, jugador_id, activo, goles, asistencias, disparos_por_partido,
                     key_passes_por_partido, dribbles_ganados_por_partido,
                     faltas_cometidas_por_partido, rating)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, estadisticasId, jugadorId, true, 3, 0, new BigDecimal("1.0"),
                pasesClave == null ? null : new BigDecimal(pasesClave),
                new BigDecimal("0.5"), new BigDecimal("0.2"), new BigDecimal("6.5"));
        jdbcTemplate.update("""
                INSERT INTO bolsas_de_jugadores
                    (id, estadisticas_deportivas_id, cotizacion_actual, fecha_actualizacion,
                     fuente, tokens_restantes)
                VALUES (?, ?, ?, ?, ?, ?)
                """, bolsaId, estadisticasId, new BigDecimal(cotizacion),
                LocalDateTime.of(2026, 1, 1, 0, 0), "test", 0);
        return bolsaId;
    }

        /** Recupera la cotización vigente de una bolsa por su identificador. */
        private BigDecimal cotizacion(UUID bolsaId) {
        return jdbcTemplate.queryForObject(
                "SELECT cotizacion_actual FROM bolsas_de_jugadores WHERE id = ?",
                BigDecimal.class, bolsaId);
    }

        /** Inserta 201 bolsas ordenadas para forzar el paso por dos páginas del servicio. */
        private void insertarMercadoNumerado() {
                var ids = IntStream.rangeClosed(1, 201).boxed().toList();
                jdbcTemplate.batchUpdate("""
                                INSERT INTO jugadores (id, whoscored_id, nombre, equipo, liga, edad)
                                VALUES (?, ?, ?, ?, ?, ?)
                                """, ids, 201, (statement, indice) -> {
                        statement.setObject(1, new UUID(0, indice));
                        statement.setInt(2, 920000 + indice);
                        statement.setString(3, "Jugador " + indice);
                        statement.setString(4, "Equipo");
                        statement.setString(5, "LaLiga");
                        statement.setInt(6, 25);
                });
                jdbcTemplate.batchUpdate("""
                                INSERT INTO estadisticas_deportivas
                                        (id, jugador_id, activo, goles, asistencias, disparos_por_partido,
                                         key_passes_por_partido, dribbles_ganados_por_partido,
                                         faltas_cometidas_por_partido, rating)
                                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                                """, ids, 201, (statement, indice) -> configurarEstadisticas(statement, indice));
                jdbcTemplate.batchUpdate("""
                                INSERT INTO bolsas_de_jugadores
                                        (id, estadisticas_deportivas_id, cotizacion_actual, fecha_actualizacion,
                                         fuente, tokens_restantes)
                                VALUES (?, ?, ?, ?, ?, ?)
                                """, ids, 201, (statement, indice) -> {
                        statement.setObject(1, new UUID(2, indice));
                        statement.setObject(2, new UUID(1, indice));
                        statement.setBigDecimal(3, indice == 201 ? BigDecimal.ONE : new BigDecimal("10.00"));
                        statement.setObject(4, LocalDateTime.of(2026, 1, 1, 0, 0));
                        statement.setString(5, "test");
                        statement.setInt(6, 0);
                });
        }

        /** Escribe parámetros estadísticos; la última fila fuerza una cotización inválida. */
        private void configurarEstadisticas(PreparedStatement statement, int indice) throws SQLException {
                statement.setObject(1, new UUID(1, indice));
                statement.setObject(2, new UUID(0, indice));
                statement.setBoolean(3, true);
                statement.setInt(4, indice == 201 ? 0 : 3);
                statement.setInt(5, 0);
                statement.setBigDecimal(6, BigDecimal.ONE);
                statement.setBigDecimal(7, BigDecimal.ONE);
                statement.setBigDecimal(8, BigDecimal.ONE);
                statement.setBigDecimal(9, BigDecimal.ONE);
                statement.setBigDecimal(10, new BigDecimal("5.0"));
        }

        /** Elimina los registros de prueba respetando el orden de las claves foráneas. */
        private void borrarMercadoNumerado() {
                jdbcTemplate.update("""
                                DELETE FROM bolsas_de_jugadores WHERE estadisticas_deportivas_id IN
                                        (SELECT e.id FROM estadisticas_deportivas e
                                         JOIN jugadores j ON j.id = e.jugador_id
                                         WHERE j.whoscored_id BETWEEN ? AND ?)
                                """, 920001, 920201);
                jdbcTemplate.update("""
                                DELETE FROM estadisticas_deportivas WHERE jugador_id IN
                                        (SELECT id FROM jugadores WHERE whoscored_id BETWEEN ? AND ?)
                                """, 920001, 920201);
                jdbcTemplate.update("DELETE FROM jugadores WHERE whoscored_id BETWEEN ? AND ?",
                                920001, 920201);
        }
}