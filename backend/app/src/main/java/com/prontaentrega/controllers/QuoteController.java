package com.prontaentrega.controllers;

import com.prontaentrega.services.QuoteRecalculationService;
import com.prontaentrega.services.QuoteStrategyAdministrationService;
import com.prontaentrega.services.dto.ResultadoRecotizacion;
import com.prontaentrega.controllers.dto.quotes.QuoteStrategiesResponse;
import com.prontaentrega.controllers.dto.quotes.QuoteStrategyConfigRequest;
import com.prontaentrega.controllers.dto.quotes.QuoteStrategyConfigResponse;
import com.prontaentrega.controllers.dto.quotes.QuoteStrategyResponse;
import com.prontaentrega.controllers.dto.quotes.QuoteStrategySelectionRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Expone la ejecución manual de recotización del mercado. */
@RestController
@RequestMapping({"/quotes", "/api/quotes"})
public class QuoteController {
    private final QuoteRecalculationService recalculationService;
    private final QuoteStrategyAdministrationService administrationService;

    /** Construye el controller con el servicio de negocio compartido con el scheduler. */
    public QuoteController(QuoteRecalculationService recalculationService,
                           QuoteStrategyAdministrationService administrationService) {
        this.recalculationService = recalculationService;
        this.administrationService = administrationService;
    }

    /** Recotiza todos los jugadores con la estrategia activa y su configuración persistida. */
    @PostMapping("/recalculate")
    public ResponseEntity<ResultadoRecotizacion> recalcularMercado() {
        return ResponseEntity.ok(recalculationService.recalcularEstrategiaActiva());
    }

    /** Devuelve la estrategia activa y su configuración sin exponer secretos. */
    @GetMapping("/strategy")
    public ResponseEntity<QuoteStrategyResponse> consultarEstrategiaActiva() {
        var estrategia = administrationService.consultarActiva();
        return ResponseEntity.ok(new QuoteStrategyResponse(estrategia.strategyId(),
                estrategia.strategyName(), estrategia.active(), estrategia.config()));
    }

    /** Devuelve estrategias disponibles y sus configuraciones persistidas. */
    @GetMapping("/strategies")
    public ResponseEntity<QuoteStrategiesResponse> consultarEstrategias() {
        List<QuoteStrategyResponse> estrategias = administrationService.listarEstrategias()
                .stream()
                .map(estrategia -> new QuoteStrategyResponse(estrategia.strategyId(),
                        estrategia.strategyName(), estrategia.active(), estrategia.config()))
                .toList();
        return ResponseEntity.ok(new QuoteStrategiesResponse(estrategias));
    }

    /** Guarda solo la configuración de la estrategia indicada; no activa ni recalcula. */
    @PutMapping("/strategies/{strategyId}/config")
    public ResponseEntity<QuoteStrategyConfigResponse> actualizarConfiguracion(
            @PathVariable String strategyId,
            @Valid @RequestBody QuoteStrategyConfigRequest request) {
        var configuracion = administrationService.actualizarConfiguracion(
                strategyId, request.aConfiguracion());
        return ResponseEntity.ok(new QuoteStrategyConfigResponse(strategyId, configuracion));
    }

    /** Activa una estrategia y devuelve el resultado de su recotización inmediata. */
    @PutMapping("/strategy")
    public ResponseEntity<ResultadoRecotizacion> activarEstrategia(
            @Valid @RequestBody QuoteStrategySelectionRequest request) {
        return ResponseEntity.ok(administrationService.activarEstrategia(request.strategyId()));
    }
}