package com.prontaentrega.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.prontaentrega.models.estrategias.ConfiguracionEstrategiaPonderada;
import com.prontaentrega.models.EstadoEstrategiaCotizacion;
import com.prontaentrega.persistence.repository.QuoteStrategyRepository;
import com.prontaentrega.utils.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/** Comprueba que un error de persistencia revierte todos los batches previos de la transacción. */
@SpringBootTest
class QuoteRecalculationTransactionTest extends AbstractIntegrationTest {
    private static final UUID FIRST_QUOTE_ID = new UUID(2, 1);
    private static final UUID LAST_QUOTE_ID = new UUID(2, 201);

    @Autowired
    private CotizacionService administrationService;

    @Autowired
    private QuoteStrategyRepository strategyRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /** Falla una actualización de la segunda página y confirma rollback del primer batch. */
    @Test
    void reviertePaginaAnteriorSiFallaUnBatchJdbcPosterior() {
        strategyRepository.guardarConfiguracion("ponderada", configuracionPonderada());
        strategyRepository.guardarEstado(new EstadoEstrategiaCotizacion("ponderada"));
        insertarMercado();
        instalarTriggerFallaTardia();

        try {
            assertThatThrownBy(() -> administrationService.recalcularEstrategiaActiva())
                    .isInstanceOf(DataAccessException.class);

            assertThat(cotizacion(FIRST_QUOTE_ID)).isEqualByComparingTo("10.00");
            assertThat(cotizacion(LAST_QUOTE_ID)).isEqualByComparingTo("10.00");
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT estrategia_activa_id FROM estado_estrategia_cotizacion WHERE id = ?",
                    String.class, "global")).isEqualTo("ponderada");
        } finally {
            removerTriggerFallaTardia();
            strategyRepository.guardarEstado(new EstadoEstrategiaCotizacion(null));
            borrarMercado();
            jdbcTemplate.update("DELETE FROM configuracion_estrategia_ponderada WHERE estrategia_id = ?",
                    "ponderada");
        }
    }

    private ConfiguracionEstrategiaPonderada configuracionPonderada() {
        return new ConfiguracionEstrategiaPonderada(BigDecimal.ONE, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, new BigDecimal("2"));
    }

    private void insertarMercado() {
        List<Integer> indices = IntStream.rangeClosed(1, 201).boxed().toList();
        jdbcTemplate.batchUpdate("""
                INSERT INTO jugadores (id, whoscored_id, nombre, equipo, liga, edad)
                VALUES (?, ?, ?, ?, ?, ?)
                """, indices, 201, (statement, indice) -> {
            statement.setObject(1, new UUID(0, indice));
            statement.setInt(2, 940000 + indice);
            statement.setString(3, "Jugador lote " + indice);
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
                """, indices, 201, (statement, indice) -> {
            statement.setObject(1, new UUID(1, indice));
            statement.setObject(2, new UUID(0, indice));
            statement.setBoolean(3, true);
            statement.setInt(4, 3);
            statement.setInt(5, 0);
            statement.setBigDecimal(6, BigDecimal.ONE);
            statement.setBigDecimal(7, BigDecimal.ONE);
            statement.setBigDecimal(8, BigDecimal.ONE);
            statement.setBigDecimal(9, BigDecimal.ONE);
            statement.setBigDecimal(10, new BigDecimal("5.0"));
        });
        jdbcTemplate.batchUpdate("""
                INSERT INTO bolsas_de_jugadores
                    (id, estadisticas_deportivas_id, cotizacion_actual, fecha_actualizacion,
                     fuente, tokens_restantes)
                VALUES (?, ?, ?, ?, ?, ?)
                """, indices, 201, (statement, indice) -> {
            statement.setObject(1, new UUID(2, indice));
            statement.setObject(2, new UUID(1, indice));
            statement.setBigDecimal(3, new BigDecimal("10.00"));
            statement.setObject(4, LocalDateTime.of(2026, 1, 1, 0, 0));
            statement.setString(5, "test");
            statement.setInt(6, 0);
        });
    }

    private void instalarTriggerFallaTardia() {
        jdbcTemplate.execute("""
                CREATE OR REPLACE FUNCTION quote_test_falla_tardia() RETURNS trigger AS $$
                BEGIN
                    IF NEW.id = '00000000-0000-0002-0000-0000000000c9'::uuid THEN
                        RAISE EXCEPTION 'Fallo de persistencia inducido por prueba';
                    END IF;
                    RETURN NEW;
                END;
                $$ LANGUAGE plpgsql
                """);
        jdbcTemplate.execute("""
                CREATE TRIGGER quote_test_falla_tardia_trigger
                BEFORE UPDATE OF cotizacion_actual ON bolsas_de_jugadores
                FOR EACH ROW EXECUTE FUNCTION quote_test_falla_tardia()
                """);
    }

    private void removerTriggerFallaTardia() {
        jdbcTemplate.execute("DROP TRIGGER IF EXISTS quote_test_falla_tardia_trigger ON bolsas_de_jugadores");
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS quote_test_falla_tardia()");
    }

    private BigDecimal cotizacion(UUID idBolsa) {
        return jdbcTemplate.queryForObject(
                "SELECT cotizacion_actual FROM bolsas_de_jugadores WHERE id = ?",
                BigDecimal.class, idBolsa);
    }

    private void borrarMercado() {
        jdbcTemplate.update("DELETE FROM bolsas_de_jugadores WHERE estadisticas_deportivas_id IN "
                        + "(SELECT id FROM estadisticas_deportivas WHERE jugador_id IN "
                        + "(SELECT id FROM jugadores WHERE whoscored_id BETWEEN ? AND ?))",
                940001, 940201);
        jdbcTemplate.update("DELETE FROM estadisticas_deportivas WHERE jugador_id IN "
                + "(SELECT id FROM jugadores WHERE whoscored_id BETWEEN ? AND ?)", 940001, 940201);
        jdbcTemplate.update("DELETE FROM jugadores WHERE whoscored_id BETWEEN ? AND ?", 940001, 940201);
    }
}