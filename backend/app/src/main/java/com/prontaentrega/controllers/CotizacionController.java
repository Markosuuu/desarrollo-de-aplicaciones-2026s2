package com.prontaentrega.controllers;

import com.prontaentrega.services.QuoteRecalculationService;
import com.prontaentrega.services.QuoteStrategyAdministrationService;
import com.prontaentrega.services.dto.ResultadoRecotizacion;
import com.prontaentrega.controllers.dtos.cotizacion.CotizacionStrategiesResponse;
import com.prontaentrega.controllers.dtos.cotizacion.CotizacionStrategyConfigRequest;
import com.prontaentrega.controllers.dtos.cotizacion.CotizacionStrategyConfigResponse;
import com.prontaentrega.controllers.dtos.cotizacion.CotizacionStrategyResponse;
import com.prontaentrega.controllers.dtos.cotizacion.CotizacionStrategySelectionRequest;
import com.prontaentrega.controllers.exceptions.CotizacionErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

@RestController
@RequestMapping({"/api/quotes"})
public class CotizacionController {
    private final QuoteRecalculationService recalculationService;
    private final QuoteStrategyAdministrationService administrationService;

    public CotizacionController(QuoteRecalculationService recalculationService,
                                QuoteStrategyAdministrationService administrationService) {
        this.recalculationService = recalculationService;
        this.administrationService = administrationService;
    }

    @Operation(
            summary = "Recotizar mercado",
            description = "Recotiza todos los jugadores utilizando la estrategia activa y su configuración persistida."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recotización completada."),
            @ApiResponse(responseCode = "409", description = "No se pudo completar la recotización por un conflicto o por una configuración incompleta.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CotizacionErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "No se pudo completar la operación por un error de persistencia.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CotizacionErrorResponse.class)))
    })
    @PostMapping("/recalculate")
    public ResponseEntity<ResultadoRecotizacion> recalcularMercado() {
        return ResponseEntity.ok(recalculationService.recalcularEstrategiaActiva());
    }

    @Operation(
            summary = "Consultar estrategia activa",
            description = "Devuelve la estrategia activa y su configuración persistida."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estrategia activa consultada."),
            @ApiResponse(responseCode = "409", description = "No hay una estrategia activa disponible o su configuración está incompleta.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CotizacionErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "No se pudo consultar la estrategia por un error de persistencia.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CotizacionErrorResponse.class)))
    })
    @GetMapping("/strategy")
    public ResponseEntity<CotizacionStrategyResponse> consultarEstrategiaActiva() {
        var estrategia = administrationService.consultarActiva();
        return ResponseEntity.ok(new CotizacionStrategyResponse(estrategia.strategyId(),
                estrategia.strategyName(), estrategia.active(), estrategia.config()));
    }

    @Operation(
            summary = "Listar estrategias",
            description = "Devuelve las estrategias disponibles, sus configuraciones persistidas y cuál está activa."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estrategias consultadas."),
            @ApiResponse(responseCode = "500", description = "No se pudieron consultar las estrategias por un error de persistencia.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CotizacionErrorResponse.class)))
    })
    @GetMapping("/strategies")
    public ResponseEntity<CotizacionStrategiesResponse> consultarEstrategias() {
        List<CotizacionStrategyResponse> estrategias = administrationService.listarEstrategias()
                .stream()
                .map(estrategia -> new CotizacionStrategyResponse(estrategia.strategyId(),
                        estrategia.strategyName(), estrategia.active(), estrategia.config()))
                .toList();
        return ResponseEntity.ok(new CotizacionStrategiesResponse(estrategias));
    }

    @Operation(
            summary = "Actualizar configuración de estrategia",
            description = "Guarda la configuración de la estrategia indicada sin activarla ni recalcular el mercado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configuración actualizada."),
            @ApiResponse(responseCode = "400", description = "El cuerpo de la solicitud no cumple el contrato de configuración.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CotizacionErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "La estrategia solicitada no existe.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CotizacionErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "No se pudo actualizar la configuración por un conflicto de estado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CotizacionErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "No se pudo guardar la configuración por un error de persistencia.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CotizacionErrorResponse.class)))
    })
    @PutMapping("/strategies/{strategyId}/config")
    public ResponseEntity<CotizacionStrategyConfigResponse> actualizarConfiguracion(
            @PathVariable String strategyId,
            @Valid @RequestBody CotizacionStrategyConfigRequest request) {
        var configuracion = administrationService.actualizarConfiguracion(
                strategyId, request.aConfiguracion());
        return ResponseEntity.ok(new CotizacionStrategyConfigResponse(strategyId, configuracion));
    }

    @Operation(
            summary = "Activar estrategia",
            description = "Activa la estrategia indicada y recotiza el mercado inmediatamente con su configuración persistida."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estrategia activada y mercado recotizado."),
            @ApiResponse(responseCode = "400", description = "El cuerpo de la solicitud no contiene un identificador de estrategia válido.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CotizacionErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "La estrategia solicitada no existe.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CotizacionErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "No se pudo activar la estrategia o recotizar por un conflicto o configuración incompleta.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CotizacionErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "No se pudo completar la operación por un error de persistencia.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CotizacionErrorResponse.class)))
    })
    @PutMapping("/strategy")
    public ResponseEntity<ResultadoRecotizacion> activarEstrategia(
            @Valid @RequestBody CotizacionStrategySelectionRequest request) {
        return ResponseEntity.ok(administrationService.activarEstrategia(request.strategyId()));
    }
}