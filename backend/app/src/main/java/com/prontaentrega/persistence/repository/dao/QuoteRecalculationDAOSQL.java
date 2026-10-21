package com.prontaentrega.persistence.repository.dao;

import com.prontaentrega.models.DatosValoracion;
import com.prontaentrega.persistence.repository.QuoteRecalculationRepository.ActualizacionCotizacion;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/** Ejecuta proyecciones escalares keyset y updates parametrizados por lote en PostgreSQL. */
@Repository
public class QuoteRecalculationDAOSQL {
    private static final String SELECT_PAGE = """
            SELECT b.id, b.cotizacion_actual, e.goles, e.asistencias,
                   e.disparos_por_partido, e.key_passes_por_partido,
                   e.dribbles_ganados_por_partido, e.faltas_cometidas_por_partido, e.rating
            FROM bolsas_de_jugadores b
            JOIN estadisticas_deportivas e ON e.id = b.estadisticas_deportivas_id
            WHERE (CAST(? AS uuid) IS NULL OR b.id > CAST(? AS uuid))
            ORDER BY b.id
            LIMIT ?
            """;

    private static final String UPDATE_QUOTE = """
            UPDATE bolsas_de_jugadores
            SET cotizacion_actual = ?
            WHERE id = ?
            """;

    private static final RowMapper<DatosValoracion> ROW_MAPPER =
            QuoteRecalculationDAOSQL::mapearValoracion;

    private final JdbcTemplate jdbcTemplate;

    /** Construye el DAO con el JdbcTemplate sincronizado con la transacción Spring. */
    public QuoteRecalculationDAOSQL(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Lee solo columnas escalares y limita el resultado al tamaño de página solicitado. */
    public List<DatosValoracion> buscarPagina(UUID cursorExclusivo, int limite) {
        return jdbcTemplate.query(SELECT_PAGE, ROW_MAPPER,
                cursorExclusivo, cursorExclusivo, limite);
    }

    /** Persiste en batch resultados previamente calculados y validados por las estrategias. */
    public int actualizarCotizaciones(List<ActualizacionCotizacion> actualizaciones) {
        int[][] resultados = jdbcTemplate.batchUpdate(UPDATE_QUOTE, actualizaciones,
                actualizaciones.size(), (statement, actualizacion) -> {
                    statement.setBigDecimal(1, actualizacion.cotizacionCalculada());
                    statement.setObject(2, actualizacion.bolsaId());
                });
        int actualizados = 0;
        for (int[] sublote : resultados) {
            for (int resultado : sublote) {
                if (resultado > 0 || resultado == java.sql.Statement.SUCCESS_NO_INFO) {
                    actualizados++;
                }
            }
        }
        return actualizados;
    }

    /** Construye una proyección escalar desde una fila JDBC sin hidratar entidades JPA. */
    private static DatosValoracion mapearValoracion(ResultSet resultado, int fila)
            throws SQLException {
        return new DatosValoracion(
                resultado.getObject("id", UUID.class),
                resultado.getBigDecimal("cotizacion_actual"),
                resultado.getObject("goles", Integer.class),
                resultado.getObject("asistencias", Integer.class),
                resultado.getBigDecimal("disparos_por_partido"),
                resultado.getBigDecimal("key_passes_por_partido"),
                resultado.getBigDecimal("dribbles_ganados_por_partido"),
                resultado.getBigDecimal("faltas_cometidas_por_partido"),
                resultado.getBigDecimal("rating"));
    }
}