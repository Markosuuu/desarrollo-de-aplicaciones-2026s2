package com.prontaentrega.services;

import com.prontaentrega.models.ConfiguracionEstrategiaCotizacion;
import com.prontaentrega.models.EstadoEstrategiaCotizacion;
import com.prontaentrega.models.EstrategiaCotizacion;
import com.prontaentrega.persistence.repository.QuoteStrategyRepository;
import com.prontaentrega.services.dto.ResultadoRecotizacion;
import com.prontaentrega.services.exceptions.QuoteOperationConflictException;
import com.prontaentrega.services.exceptions.QuoteRequestValidationException;
import com.prontaentrega.services.exceptions.QuoteStrategyNotFoundException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** Coordina lectura, persistencia y activación de estrategias sin implementar sus fórmulas. */
@Service
public class QuoteStrategyAdministrationService {
    private final QuoteStrategyRepository strategyRepository;
    private final QuoteRecalculationService recalculationService;
    private final Map<String, EstrategiaCotizacion> strategiesById;

    /** Registra estrategias y comprueba su asociación explícita con providers tipados. */
    public QuoteStrategyAdministrationService(QuoteStrategyRepository strategyRepository,
                                              QuoteRecalculationService recalculationService,
                                              List<EstrategiaCotizacion> strategies) {
        this.strategyRepository = strategyRepository;
        this.recalculationService = recalculationService;
        this.strategiesById = new HashMap<>();
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
        return recalculationService.recalcularEstrategiaActiva();
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
}