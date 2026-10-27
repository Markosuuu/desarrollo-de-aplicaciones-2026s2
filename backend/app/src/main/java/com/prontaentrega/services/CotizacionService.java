package com.prontaentrega.services;

import com.prontaentrega.models.ConfiguracionEstrategiaCotizacion;
import com.prontaentrega.models.DatosValoracion;
import com.prontaentrega.models.EstadoEstrategiaCotizacion;
import com.prontaentrega.models.EstrategiaCotizacion;
import com.prontaentrega.models.exception.DatosValoracionInvalidaException;
import com.prontaentrega.persistence.repository.CotizacionRepository;
import com.prontaentrega.persistence.repository.CotizacionRepository.ActualizacionCotizacion;
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

    private final CotizacionRepository cotizacionRepository;
    private final Map<String, EstrategiaCotizacion> estrategias;

    /**
     * Constructor del servicio de cotización.
     * @param cotizacionRepository
     * @param estrategias
     */
    public CotizacionService(CotizacionRepository cotizacionRepository,
                             List<EstrategiaCotizacion> estrategias) {
        this.cotizacionRepository = cotizacionRepository;
        this.estrategias = new HashMap<>();

        validacionesConstructor(cotizacionRepository, estrategias);
    }

    private void validacionesConstructor(CotizacionRepository cotizacionRepository, List<EstrategiaCotizacion> estrategias) {
        for (EstrategiaCotizacion estrategia : estrategias) {
            EstrategiaCotizacion previous = this.estrategias.put(estrategia.estrategiaId(), estrategia);

            if (previous != null) {
                throw new IllegalStateException("ID de estrategia duplicado: " + estrategia.estrategiaId());
            }

            if (!estrategia.tipoConfiguracion().equals(
                    cotizacionRepository.tipoConfiguracion(estrategia.estrategiaId()))) {
                throw new IllegalStateException(
                        "El provider no coincide con la configuración de " + estrategia.estrategiaId());
            }
        }

        if (!this.estrategias.keySet().equals(cotizacionRepository.estrategiaIdsConfigurables())) {
            throw new IllegalStateException(
                    "Los providers de configuración deben corresponder uno a uno con las estrategias");
        }
    }

    /** Recalcula todas las bolsas con la estrategia activa en un snapshot transaccional. */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ResultadoRecotizacion recalcularEstrategiaActiva() {
        // estrategia activa
        // DatosActualizacion datos =



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
        String activeId = cotizacionRepository.buscarEstado()
                .map(EstadoEstrategiaCotizacion::estrategiaActivaId)
                .orElse(null);
        if (activeId == null) {
            throw new QuoteOperationConflictException("No hay una estrategia activa configurada");
        }
        EstrategiaCotizacion strategy = estrategias.get(activeId);
        if (strategy == null) {
            throw new QuoteOperationConflictException("La estrategia activa no está disponible");
        }
        ConfiguracionEstrategiaCotizacion config = buscarConfiguracion(strategy);

        return new QuoteStrategyView(strategy.estrategiaId(), strategy.nombreEstrategia(), true, config);
    }

    /** Devuelve las estrategias registradas y las configuraciones independientes disponibles. */
    @Transactional(readOnly = true)
    public List<QuoteStrategyView> listarEstrategias() {
        String activeId = cotizacionRepository.buscarEstado()
                .map(EstadoEstrategiaCotizacion::estrategiaActivaId)
                .orElse(null);
        return estrategias.values().stream()
                .sorted(Comparator.comparing(EstrategiaCotizacion::estrategiaId))
                .map(strategy -> new QuoteStrategyView(strategy.estrategiaId(),
                        strategy.nombreEstrategia(),
                        strategy.estrategiaId().equals(activeId),
                        cotizacionRepository.buscarConfiguracion(strategy.estrategiaId()).orElse(null)))
                .toList();
    }

    /** Guarda configuración sin modificar selección activa ni recalcular el mercado. */
    @Transactional
    public ConfiguracionEstrategiaCotizacion actualizarConfiguracion(
            String strategyId, ConfiguracionEstrategiaCotizacion configuracion) {

        EstrategiaCotizacion strategy = buscarEstrategia(strategyId);
        validarTipoConfiguracion(strategy, configuracion);
        cotizacionRepository.bloquearEstado().orElseThrow(() ->
                new QuoteOperationConflictException("El estado de estrategias no está inicializado"));
        cotizacionRepository.guardarConfiguracion(strategyId, configuracion);
        return configuracion;
    }

    /** Activa una estrategia y ejecuta la recotización dentro de una transacción única. */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ResultadoRecotizacion activarEstrategia(String strategyId) {
        EstrategiaCotizacion strategy = buscarEstrategia(strategyId);
        cotizacionRepository.bloquearEstado().orElseThrow(() ->
                new QuoteOperationConflictException("El estado de estrategias no está inicializado"));
        buscarConfiguracion(strategy);
        cotizacionRepository.guardarEstado(new EstadoEstrategiaCotizacion(strategy.estrategiaId()));
        return recalcularEstrategiaActiva();
    }

    private EstrategiaActiva obtenerEstrategiaActiva() {
        EstadoEstrategiaCotizacion estado = cotizacionRepository.bloquearEstado()
                .orElseThrow(() -> new QuoteOperationConflictException(
                        "No hay una estrategia activa configurada"));
        String strategyId = estado.estrategiaActivaId();
        if (strategyId == null) {
            throw new QuoteOperationConflictException(
                    "No hay una estrategia activa configurada");
        }

        EstrategiaCotizacion strategy = estrategias.get(strategyId);
        if (strategy == null) {
            throw new QuoteOperationConflictException(
                    "La estrategia activa no está disponible");
        }

        ConfiguracionEstrategiaCotizacion configuracion = cotizacionRepository
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
            List<DatosValoracion> page = cotizacionRepository.buscarPagina(cursor, PAGE_SIZE);
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
        int updatedPlayers = cotizacionRepository.actualizarCotizaciones(updates);
        if (updatedPlayers != updates.size()) {
            throw new QuoteOperationConflictException(
                    "No se pudieron actualizar todas las cotizaciones calculadas");
        }
        return updatedPlayers;
    }

    /** Resuelve una estrategia registrada o devuelve el error funcional de recurso inexistente. */
    private EstrategiaCotizacion buscarEstrategia(String strategyId) {
        EstrategiaCotizacion strategy = estrategias.get(strategyId);
        if (strategy == null) {
            throw new QuoteStrategyNotFoundException("La estrategia solicitada no existe");
        }
        return strategy;
    }

    /** Recupera y valida la configuración persistida contra el tipo declarado por la Strategy. */
    private ConfiguracionEstrategiaCotizacion buscarConfiguracion(EstrategiaCotizacion strategy) {
        ConfiguracionEstrategiaCotizacion configuracion = cotizacionRepository
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