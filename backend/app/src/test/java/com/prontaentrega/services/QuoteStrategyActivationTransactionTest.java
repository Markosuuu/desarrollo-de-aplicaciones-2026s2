package com.prontaentrega.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.prontaentrega.models.estrategias.ConfiguracionEstrategiaPonderada;
import com.prontaentrega.models.estrategias.ConfiguracionEstrategiaPonderadaPenalizacion;
import com.prontaentrega.models.EstadoEstrategiaCotizacion;
import com.prontaentrega.models.exception.CotizacionInvalidaException;
import com.prontaentrega.persistence.repository.QuoteStrategyRepository;
import com.prontaentrega.utils.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/** Verifica rollback de la selección activa al fallar la recotización disparada por activación. */
@SpringBootTest
class QuoteStrategyActivationTransactionTest extends AbstractIntegrationTest {
    @Autowired
    private CotizacionService administrationService;

    @Autowired
    private QuoteStrategyRepository strategyRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /** Conserva estrategia previa y cotización si la nueva estrategia produce un valor inválido. */
    @Test
    void revierteSeleccionActivaYCotizacionAlFallarActivacion() {
        ConfiguracionEstrategiaPonderada ponderada = new ConfiguracionEstrategiaPonderada(
                BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE);
        ConfiguracionEstrategiaPonderadaPenalizacion penalizada =
                new ConfiguracionEstrategiaPonderadaPenalizacion(
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE);
        UUID jugadorId = UUID.randomUUID();
        UUID estadisticasId = UUID.randomUUID();
        UUID bolsaId = UUID.randomUUID();
        insertarBolsa(jugadorId, estadisticasId, bolsaId);
        strategyRepository.guardarConfiguracion("ponderada", ponderada);
        strategyRepository.guardarConfiguracion("ponderada-con-penalizacion", penalizada);
        strategyRepository.guardarEstado(new EstadoEstrategiaCotizacion("ponderada"));

        try {
            assertThatThrownBy(() -> administrationService.activarEstrategia(
                    "ponderada-con-penalizacion"))
                    .isInstanceOf(CotizacionInvalidaException.class);

            assertThat(jdbcTemplate.queryForObject(
                    "SELECT estrategia_activa_id FROM estado_estrategia_cotizacion WHERE id = ?",
                    String.class, "global")).isEqualTo("ponderada");
            assertThat(cotizacion(bolsaId)).isEqualByComparingTo("1.10");
        } finally {
            strategyRepository.guardarEstado(new EstadoEstrategiaCotizacion(null));
            jdbcTemplate.update("DELETE FROM bolsas_de_jugadores WHERE id = ?", bolsaId);
            jdbcTemplate.update("DELETE FROM estadisticas_deportivas WHERE id = ?", estadisticasId);
            jdbcTemplate.update("DELETE FROM jugadores WHERE id = ?", jugadorId);
            jdbcTemplate.update("DELETE FROM configuracion_estrategia_ponderada WHERE estrategia_id = ?",
                    "ponderada");
            jdbcTemplate.update("DELETE FROM configuracion_estrategia_ponderada_penalizacion WHERE estrategia_id = ?",
                    "ponderada-con-penalizacion");
        }
    }

    private void insertarBolsa(UUID jugadorId, UUID estadisticasId, UUID bolsaId) {
        jdbcTemplate.update("""
                INSERT INTO jugadores (id, whoscored_id, nombre, equipo, liga, edad)
                VALUES (?, ?, ?, ?, ?, ?)
                """, jugadorId, 930001, "Jugador rollback", "Equipo", "LaLiga", 25);
        jdbcTemplate.update("""
                INSERT INTO estadisticas_deportivas
                    (id, jugador_id, activo, goles, asistencias, disparos_por_partido,
                     key_passes_por_partido, dribbles_ganados_por_partido,
                     faltas_cometidas_por_partido, rating)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, estadisticasId, jugadorId, true, 3, 0, BigDecimal.ONE, BigDecimal.ONE,
                new BigDecimal("0.5"), new BigDecimal("0.2"), new BigDecimal("6.5"));
        jdbcTemplate.update("""
                INSERT INTO bolsas_de_jugadores
                    (id, estadisticas_deportivas_id, cotizacion_actual, fecha_actualizacion,
                     fuente, tokens_restantes)
                VALUES (?, ?, ?, ?, ?, ?)
                """, bolsaId, estadisticasId, new BigDecimal("1.10"),
                LocalDateTime.of(2026, 1, 1, 0, 0), "test", 0);
    }

    private BigDecimal cotizacion(UUID bolsaId) {
        return jdbcTemplate.queryForObject(
                "SELECT cotizacion_actual FROM bolsas_de_jugadores WHERE id = ?",
                BigDecimal.class, bolsaId);
    }
}