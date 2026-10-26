package com.prontaentrega.services;

import com.prontaentrega.models.ConfiguracionEstrategiaCotizacion;
import com.prontaentrega.models.DatosValoracion;
import com.prontaentrega.models.EstadoEstrategiaCotizacion;
import com.prontaentrega.models.EstrategiaCotizacion;
import com.prontaentrega.models.exception.DatosValoracionInvalidaException;
import com.prontaentrega.persistence.repository.QuoteRecalculationRepository;
import com.prontaentrega.persistence.repository.QuoteRecalculationRepository.ActualizacionCotizacion;
import com.prontaentrega.persistence.repository.QuoteStrategyRepository;
import com.prontaentrega.services.dto.ResultadoRecotizacion;
import com.prontaentrega.services.exceptions.QuoteOperationConflictException;
import com.prontaentrega.services.exceptions.QuoteRequestValidationException;
import com.prontaentrega.services.exceptions.QuoteStrategyNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio encargado de coordinar las estrategias y la
 * cotizacion de los jugadores
 */
@Service
public class CotizacionService {
    private static final int PAGE_SIZE = 200;

    private final QuoteStrategyRepository strategyRepository;
    private final QuoteRecalculationRepository quoteRepository;
    private final Map<String, EstrategiaCotizacion> strategiesById;

    /**
     * Constructor del servicio de cotización.
     * @param strategyRepository
     * @param quoteRepository
     * @param strategies
     */
    public CotizacionService(QuoteStrategyRepository strategyRepository,
                             QuoteRecalculationRepository quoteRepository,
                             List<EstrategiaCotizacion> strategies) {
        this.strategyRepository = strategyRepository;
        this.quoteRepository = quoteRepository;
        this.strategiesById = new HashMap<>();

        validacionesConstructor(strategyRepository, strategies);
    }

    private void validacionesConstructor(QuoteStrategyRepository strategyRepository, List<EstrategiaCotizacion> strategies) {
        for (EstrategiaCotizacion strategy : strategies) {
            EstrategiaCotizacion previous = strategiesById.put(strategy.estrategiaId(), strategy);

            if (previous != null) {
                throw new IllegalStateException("ID de estrategia duplicado: " + strategy.estrategiaId());
            }

            if (!strategy.tipoConfiguracion().equals(
                    strategyRepository.tipoConfiguracion(strategy.estrategiaId()))) {
                throw new IllegalStateException(
                        "El provider no coincide con la configuración de " + strategy.estrategiaId());
            }
        }

        if (!strategiesById.keySet().equals(strategyRepository.estrategiaIdsConfigurables())) {
            throw new IllegalStateException(
                    "Los providers de configuración deben corresponder uno a uno con las estrategias");
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

    /** Devuelve la selección activa y su configuración persistida. */
    @Transactional(readOnly = true)
    public QuoteStrategyView consultarActiva() {
        String activeId = strategyRepository.buscarEstado()
                .map(EstadoEstrategiaCotizacion::estrategiaActivaId)
                .orElse(null);
        if (activeId == null) {
            throw new QuoteOperationConflictException("No hay una estrategia activa configurada");
        }
        EstrategiaCotizacion strategy = strategiesById.get(activeId);
        if (strategy == null) {
            throw new QuoteOperationConflictException("La estrategia activa no está disponible");
        }
        ConfiguracionEstrategiaCotizacion config = buscarConfiguracion(strategy);
        return new QuoteStrategyView(strategy.estrategiaId(), strategy.nombreEstrategia(), true, config);
    }

    /** Devuelve las estrategias registradas y las configuraciones independientes disponibles. */
    @Transactional(readOnly = true)
    public List<QuoteStrategyView> listarEstrategias() {
        String activeId = strategyRepository.buscarEstado()
                .map(EstadoEstrategiaCotizacion::estrategiaActivaId)
                .orElse(null);
        return strategiesById.values().stream()
                .sorted(Comparator.comparing(EstrategiaCotizacion::estrategiaId))
                .map(strategy -> new QuoteStrategyView(strategy.estrategiaId(),
                        strategy.nombreEstrategia(),
                        strategy.estrategiaId().equals(activeId),
                        strategyRepository.buscarConfiguracion(strategy.estrategiaId()).orElse(null)))
                .toList();
    }

    /** Guarda configuración sin modificar selección activa ni recalcular el mercado. */
    @Transactional
    public ConfiguracionEstrategiaCotizacion actualizarConfiguracion(
            String strategyId, ConfiguracionEstrategiaCotizacion configuracion) {

        EstrategiaCotizacion strategy = buscarEstrategia(strategyId);
        validarTipoConfiguracion(strategy, configuracion);
        strategyRepository.bloquearEstado().orElseThrow(() ->
                new QuoteOperationConflictException("El estado de estrategias no está inicializado"));
        strategyRepository.guardarConfiguracion(strategyId, configuracion);
        return configuracion;
    }

    /** Activa una estrategia y ejecuta la recotización dentro de una transacción única. */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ResultadoRecotizacion activarEstrategia(String strategyId) {
        EstrategiaCotizacion strategy = buscarEstrategia(strategyId);
        strategyRepository.bloquearEstado().orElseThrow(() ->
                new QuoteOperationConflictException("El estado de estrategias no está inicializado"));
        buscarConfiguracion(strategy);
        strategyRepository.guardarEstado(new EstadoEstrategiaCotizacion(strategy.estrategiaId()));
        return recalcularEstrategiaActiva();
    }

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
            skippedPlayers += page.size() - updates.size();
            if (!updates.isEmpty()) {
                processedPlayers += persistirActualizaciones(updates);
            }
            cursor = page.getLast().bolsaId();
        }
        return new int[]{processedPlayers, skippedPlayers};
    }

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
                // Los datos inválidos se omiten.
            }
        }
        return updates;
    }

    private int persistirActualizaciones(List<ActualizacionCotizacion> updates) {
        int updatedPlayers = quoteRepository.actualizarCotizaciones(updates);
        if (updatedPlayers != updates.size()) {
            throw new QuoteOperationConflictException(
                    "No se pudieron actualizar todas las cotizaciones calculadas");
        }
        return updatedPlayers;
    }

    /** Resuelve una estrategia registrada o devuelve el error funcional de recurso inexistente. */
    private EstrategiaCotizacion buscarEstrategia(String strategyId) {
        EstrategiaCotizacion strategy = strategiesById.get(strategyId);
        if (strategy == null) {
            throw new QuoteStrategyNotFoundException("La estrategia solicitada no existe");
        }
        return strategy;
    }

    /** Recupera y valida la configuración persistida contra el tipo declarado por la Strategy. */
    private ConfiguracionEstrategiaCotizacion buscarConfiguracion(EstrategiaCotizacion strategy) {
        ConfiguracionEstrategiaCotizacion configuracion = strategyRepository
                .buscarConfiguracion(strategy.estrategiaId())
                .orElseThrow(() -> new QuoteOperationConflictException(
                        "La configuración de la estrategia está incompleta"));
        validarTipoConfiguracion(strategy, configuracion);
        return configuracion;
    }

    /** Asegura que el tipo de configuración registrado coincida con el algoritmo solicitado. */
    private void validarTipoConfiguracion(EstrategiaCotizacion strategy,
                                          ConfiguracionEstrategiaCotizacion configuracion) {
        if (configuracion == null || !strategy.tipoConfiguracion().isInstance(configuracion)) {
            throw new QuoteRequestValidationException(
                    "La configuración no corresponde a la estrategia solicitada");
        }
    }

    /** Vista de administración para respuesta HTTP y verificación de estado. */
    public record QuoteStrategyView(String strategyId, String strategyName,
                                    boolean active, Object config) {
    }

    private record EstrategiaActiva(
            String strategyId,
            EstrategiaCotizacion strategy,
            ConfiguracionEstrategiaCotizacion configuracion) {
    }
}