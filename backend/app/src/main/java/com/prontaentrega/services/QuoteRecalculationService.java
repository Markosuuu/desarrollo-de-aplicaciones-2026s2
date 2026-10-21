package com.prontaentrega.services;

import com.prontaentrega.models.ConfiguracionEstrategiaCotizacion;
import com.prontaentrega.models.DatosValoracion;
import com.prontaentrega.models.EstadoEstrategiaCotizacion;
import com.prontaentrega.models.exception.DatosValoracionInvalidaException;
import com.prontaentrega.models.estrategias.EstrategiaCotizacion;
import com.prontaentrega.persistence.repository.QuoteRecalculationRepository;
import com.prontaentrega.persistence.repository.QuoteRecalculationRepository.ActualizacionCotizacion;
import com.prontaentrega.persistence.repository.QuoteStrategyRepository;
import com.prontaentrega.services.dto.ResultadoRecotizacion;
import com.prontaentrega.services.exceptions.QuoteOperationConflictException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** Orquesta la recotización completa sin incluir las fórmulas de cada estrategia. */
@Service
public class QuoteRecalculationService {
    private static final int PAGE_SIZE = 200;

    private final QuoteStrategyRepository strategyRepository;
    private final QuoteRecalculationRepository quoteRepository;
    private final Map<String, EstrategiaCotizacion> strategiesById;

    /** Construye el servicio con persistencia y estrategias registradas por identificador. */
    public QuoteRecalculationService(QuoteStrategyRepository strategyRepository,
                                     QuoteRecalculationRepository quoteRepository,
                                     List<EstrategiaCotizacion> strategies) {
        this.strategyRepository = strategyRepository;
        this.quoteRepository = quoteRepository;
        this.strategiesById = new HashMap<>();
        for (EstrategiaCotizacion strategy : strategies) {
            EstrategiaCotizacion previo = this.strategiesById.put(strategy.estrategiaId(), strategy);
            if (previo != null) {
                throw new IllegalStateException(
                        "Hay más de una estrategia registrada con ID " + strategy.estrategiaId());
            }
        }
    }

    /** Recalcula todas las bolsas con la estrategia activa en un snapshot transaccional. */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ResultadoRecotizacion recalcularEstrategiaActiva() {
        EstrategiaActiva estrategiaActiva = obtenerEstrategiaActiva();

        int[] contadores = procesarTodasLasPaginas(
                estrategiaActiva.strategy(),
                estrategiaActiva.configuracion()
        );

        return new ResultadoRecotizacion(
                estrategiaActiva.strategyId(),
                contadores[0],
                contadores[1]
        );
    }

    /** Recupera y valida la estrategia activa y su configuración persistida. */
    private EstrategiaActiva obtenerEstrategiaActiva() {
        EstadoEstrategiaCotizacion estado = strategyRepository.bloquearEstado()
                .orElseThrow(() -> new QuoteOperationConflictException(
                        "No hay una estrategia activa configurada"));

        String strategyId = estado.estrategiaActivaId();
        if (strategyId == null) {
            throw new QuoteOperationConflictException(
                    "No hay una estrategia activa configurada");
        }

        EstrategiaCotizacion strategy = strategiesById.get(strategyId);
        if (strategy == null) {
            throw new QuoteOperationConflictException(
                    "La estrategia activa no está disponible");
        }

        ConfiguracionEstrategiaCotizacion configuracion = strategyRepository
                .buscarConfiguracion(strategyId)
                .orElseThrow(() -> new QuoteOperationConflictException(
                        "La configuración de la estrategia activa está incompleta"));

        if (!strategy.tipoConfiguracion().isInstance(configuracion)) {
            throw new QuoteOperationConflictException(
                    "La configuración persistida no corresponde a la estrategia activa");
        }

        return new EstrategiaActiva(strategyId, strategy, configuracion);
    }

    /** Recorre todas las páginas y acumula los resultados de la recotización. */
    private int[] procesarTodasLasPaginas(
            EstrategiaCotizacion strategy,
            ConfiguracionEstrategiaCotizacion configuracion) {

        UUID cursor = null;
        int processedPlayers = 0;
        int skippedPlayers = 0;

        while (true) {
            List<DatosValoracion> page = quoteRepository.buscarPagina(cursor, PAGE_SIZE);
            if (page.isEmpty()) {
                break;
            }

            List<ActualizacionCotizacion> updates =
                    calcularActualizaciones(page, strategy, configuracion);

            skippedPlayers += contarOmitidos(page, updates);

            if (!updates.isEmpty()) {
                processedPlayers += persistirActualizaciones(updates);
            }

            cursor = page.getLast().bolsaId();
        }

        return new int[]{processedPlayers, skippedPlayers};
    }

    /** Calcula las cotizaciones válidas de una página. */
    private List<ActualizacionCotizacion> calcularActualizaciones(
            List<DatosValoracion> page,
            EstrategiaCotizacion strategy,
            ConfiguracionEstrategiaCotizacion configuracion) {

        List<ActualizacionCotizacion> updates = new ArrayList<>(page.size());

        for (DatosValoracion datos : page) {
            try {
                BigDecimal cotizacion = strategy.calcular(datos, configuracion);
                updates.add(new ActualizacionCotizacion(datos.bolsaId(), cotizacion));
            } catch (DatosValoracionInvalidaException ignored) {

            }
        }

        return updates;
    }

    /** Cuenta los jugadores de la página cuya cotización no pudo calcularse. */
    private int contarOmitidos(
            List<DatosValoracion> page,
            List<ActualizacionCotizacion> updates) {
        return page.size() - updates.size();
    }

    /** Persiste las actualizaciones y verifica que todas se hayan aplicado. */
    private int persistirActualizaciones(List<ActualizacionCotizacion> updates) {
        int updatedPlayers = quoteRepository.actualizarCotizaciones(updates);

        if (updatedPlayers != updates.size()) {
            throw new QuoteOperationConflictException(
                    "No se pudieron actualizar todas las cotizaciones calculadas");
        }

        return updatedPlayers;
    }

    /** Agrupa la estrategia activa y su configuración validada. */
    private record EstrategiaActiva(
            String strategyId,
            EstrategiaCotizacion strategy,
            ConfiguracionEstrategiaCotizacion configuracion) {
    }

}