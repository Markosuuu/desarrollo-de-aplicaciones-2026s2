package com.prontaentrega.services;

import com.prontaentrega.models.cotizacion.*;
import com.prontaentrega.models.exception.DatosValoracionInvalidaException;
import com.prontaentrega.models.jugadores.BolsaDeJugador;
import com.prontaentrega.persistence.repository.CotizacionRepository;
import com.prontaentrega.persistence.repository.CotizacionRepository.ActualizacionCotizacion;
import com.prontaentrega.persistence.repository.HistorialCotizacionRepository;
import com.prontaentrega.persistence.repository.JugadorRepository;
import com.prontaentrega.services.dto.ResultadoRecotizacion;
import com.prontaentrega.services.exceptions.HistorialNoEncontradoException;
import com.prontaentrega.services.exceptions.cotizacion.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CotizacionService {
    private static final int PAGE_SIZE = 200;

    private final CotizacionRepository cotizacionRepository;
    private final HistorialCotizacionRepository historialRepository;
    private final JugadorRepository jugadorRepository;
    private final Map<String, EstrategiaCotizacion> estrategias;

    public CotizacionService(CotizacionRepository cotizacionRepository,
                             HistorialCotizacionRepository historialRepository,
                             JugadorRepository jugadorRepository,
                             List<EstrategiaCotizacion> estrategias) throws EstrategiasInconsistentesException {
        this.cotizacionRepository = cotizacionRepository;
        this.historialRepository = historialRepository;
        this.jugadorRepository = jugadorRepository;
        this.estrategias = new HashMap<>();

        validacionesConstructor(cotizacionRepository, estrategias);
    }

    private void validacionesConstructor(CotizacionRepository cotizacionRepository, List<EstrategiaCotizacion> estrategias) throws EstrategiasInconsistentesException {
        for (EstrategiaCotizacion estrategia : estrategias) {
            EstrategiaCotizacion previous = this.estrategias.put(estrategia.estrategiaId(), estrategia);

            if (previous != null) {
                throw new EstrategiaDuplicadaException("Estrategia repetida: " + estrategia.estrategiaId());
            }

            if (!estrategia.tipoConfiguracion().equals(
                    cotizacionRepository.tipoConfiguracion(estrategia.estrategiaId()))) {
                throw new EstrategiasInconsistentesException(
                        "El provider no coincide con la configuración de " + estrategia.estrategiaId());
            }
        }

        if (!this.estrategias.keySet().equals(cotizacionRepository.estrategiaIdsConfigurables())) {
            throw new InconsistenciaDeEstrategiasConfigurablesException(
                    "Los providers de configuración deben corresponder uno a uno con las estrategias");
        }
    }

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
    public EstrategiaEstado consultarActiva() {
        String activeId = cotizacionRepository.buscarEstado()
                .map(EstadoEstrategiaCotizacion::estrategiaActivaId)
                .orElse(null);

        if (activeId == null) {
            throw new NoHayEstrategiaActivaConfiguradaException("No hay una estrategia activa configurada");
        }

        EstrategiaCotizacion strategy = estrategias.get(activeId);
        if (strategy == null) {
            throw new EstrategiaActivaNoDisponibleException("La estrategia activa no está disponible");
        }

        ConfiguracionEstrategiaCotizacion config = buscarConfiguracion(strategy);

        return new EstrategiaEstado(strategy.estrategiaId(), strategy.nombreEstrategia(), true, config);
    }

    /** Devuelve las estrategias registradas y las configuraciones independientes disponibles. */
    @Transactional(readOnly = true)
    public List<EstrategiaEstado> listarEstrategias() {
        String activeId = cotizacionRepository.buscarEstado()
                .map(EstadoEstrategiaCotizacion::estrategiaActivaId)
                .orElse(null);

        return estrategias.values().stream()
                .sorted(Comparator.comparing(EstrategiaCotizacion::estrategiaId))
                .map(strategy -> new EstrategiaEstado(strategy.estrategiaId(),
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
                new NoHayEstrategiaActivaConfiguradaException("El estado de estrategias no está inicializado"));

        cotizacionRepository.guardarConfiguracion(strategyId, configuracion);
        return configuracion;
    }

    /** Activa una estrategia y ejecuta la recotización dentro de una transacción única. */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ResultadoRecotizacion activarEstrategia(String strategyId) {
        EstrategiaCotizacion strategy = buscarEstrategia(strategyId);
        cotizacionRepository.bloquearEstado().orElseThrow(() ->
                new NoHayEstrategiaActivaConfiguradaException("El estado de estrategias no está inicializado"));
        buscarConfiguracion(strategy);
        cotizacionRepository.guardarEstado(new EstadoEstrategiaCotizacion(strategy.estrategiaId()));
        return recalcularEstrategiaActiva();
    }

    private EstrategiaActiva obtenerEstrategiaActiva() {
        EstadoEstrategiaCotizacion estado = cotizacionRepository.bloquearEstado()
                .orElseThrow(() -> new NoHayEstrategiaActivaConfiguradaException(
                        "No hay una estrategia activa configurada"));
        String strategyId = estado.estrategiaActivaId();
        if (strategyId == null) {
            throw new NoHayEstrategiaActivaConfiguradaException(
                    "No hay una estrategia activa configurada");
        }

        EstrategiaCotizacion strategy = estrategias.get(strategyId);
        if (strategy == null) {
            throw new NoHayEstrategiaActivaConfiguradaException(
                    "La estrategia activa no está disponible");
        }

        ConfiguracionEstrategiaCotizacion configuracion = cotizacionRepository
                .buscarConfiguracion(strategyId)
                .orElseThrow(() -> new NoHayEstrategiaActivaConfiguradaException(
                        "La configuración de la estrategia activa está incompleta"));
        if (!strategy.tipoConfiguracion().isInstance(configuracion)) {
            throw new NoHayEstrategiaActivaConfiguradaException(
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
            throw new NoHayEstrategiaActivaConfiguradaException(
                    "No se pudieron actualizar todas las cotizaciones calculadas");
        }

        LocalDateTime fechaActualizacion = LocalDateTime.now(ZoneId.of("America/Argentina/Buenos_Aires"));

        List<HistorialCotizacion> historial = updates.stream()
                .map(update -> new HistorialCotizacion(
                        null,
                        update.bolsaId(),
                        update.cotizacionCalculada(),
                        fechaActualizacion
                ))
                .toList();

        historialRepository.guardarHistorial(historial);

        return updatedPlayers;
    }

    /** Resuelve una estrategia registrada o devuelve el error funcional de recurso inexistente. */
    private EstrategiaCotizacion buscarEstrategia(String strategyId) {
        EstrategiaCotizacion strategy = estrategias.get(strategyId);
        if (strategy == null) {
            throw new EstrategiaNoExistenteException("La estrategia solicitada no existe");
        }
        return strategy;
    }

    /** Recupera y valida la configuración persistida contra el tipo declarado por la Strategy. */
    private ConfiguracionEstrategiaCotizacion buscarConfiguracion(EstrategiaCotizacion strategy) {
        ConfiguracionEstrategiaCotizacion configuracion = cotizacionRepository
                .buscarConfiguracion(strategy.estrategiaId())
                .orElseThrow(() -> new ConfiguracionIncompletaException(
                        "La configuración de la estrategia está incompleta"));

        validarTipoConfiguracion(strategy, configuracion);

        return configuracion;
    }

    /** Asegura que el tipo de configuración registrado coincida con el algoritmo solicitado. */
    private void validarTipoConfiguracion(EstrategiaCotizacion strategy,
                                          ConfiguracionEstrategiaCotizacion configuracion) {
        if (configuracion == null || !strategy.tipoConfiguracion().isInstance(configuracion)) {
            throw new ConfiguracionNoValidaParaEstrategiaException(
                    "La configuración no corresponde a la estrategia solicitada");
        }
    }

    public record EstrategiaEstado(String strategyId, String strategyName,
                                   boolean active, Object config) {
    }

    private record EstrategiaActiva(
            String strategyId,
            EstrategiaCotizacion strategy,
            ConfiguracionEstrategiaCotizacion configuracion) {
    }


    public List<HistorialCotizacion> buscarHistorialById(UUID jugadorId) {
        return historialRepository.buscarHistorialById(jugadorId);
    }

    public HistorialCotizacion buscarHistorialPorFecha(
            UUID jugadorId,
            LocalDate fecha) {
        BolsaDeJugador bolsa = jugadorRepository.findById(jugadorId).get();

        LocalDateTime inicioDia = fecha.atStartOfDay();

        Optional<HistorialCotizacion> anterior =
                historialRepository
                        .findFirstByBolsaIdAndFechaLessThanOrderByFechaDesc(
                                bolsa.getId(),
                                inicioDia
                        );

        Optional<HistorialCotizacion> posterior =
                historialRepository
                        .findFirstByBolsaIdAndFechaGreaterThanEqualOrderByFechaAsc(
                                bolsa.getId(),
                                inicioDia
                        );

        if (anterior.isEmpty() && posterior.isEmpty()) {
            throw new HistorialNoEncontradoException("El jugador no tiene historial de cotizaciones");
        }

        if (anterior.isEmpty()) {
            return posterior.get();
        }

        if (posterior.isEmpty()) {
            return anterior.get();
        }

        long distanciaAnterior = Math.abs(
                ChronoUnit.DAYS.between(
                        fecha,
                        anterior.get().getFecha().toLocalDate()
                )
        );

        long distanciaPosterior = Math.abs(
                ChronoUnit.DAYS.between(
                        fecha,
                        posterior.get().getFecha().toLocalDate()
                )
        );

        return distanciaAnterior <= distanciaPosterior
                ? anterior.get()
                : posterior.get();
    }
}