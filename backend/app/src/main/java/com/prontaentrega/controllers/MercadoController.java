package com.prontaentrega.controllers;

import com.prontaentrega.controllers.dtos.PageResponse;
import com.prontaentrega.controllers.dtos.mercado.*;
import com.prontaentrega.models.mercado.AnuncioVenta;
import com.prontaentrega.services.MercadoService;

import com.prontaentrega.services.dto.ResumenInventarioDineroResponse;
import com.prontaentrega.services.dto.ResumenInventarioTokensResponse;
import jakarta.validation.Valid;
import java.util.*;

import com.prontaentrega.controllers.exceptions.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MercadoController {
    private final MercadoService mercadoService;

    public MercadoController(MercadoService mercadoService) {
        this.mercadoService = mercadoService;
    }

    @Operation(summary = "Listar anuncios de venta", description = "Retorna los anuncios pendientes del marketplace de manera paginada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Anuncios consultados correctamente.")
    })
    @GetMapping("/marketplace/listings")
    public ResponseEntity<PageResponse<AnuncioVentaResponse>> getAnuncios(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AnuncioVentaResponse> anuncios = mercadoService
                .getAnuncios(page, size)
                .map(AnuncioVentaResponse::from);

        return ResponseEntity.ok(PageResponse.from(anuncios));
    }

    @Operation(summary = "Publicar venta de tokens", description = "Crea un anuncio para vender tokens de un jugador.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Anuncio de venta creado."),
            @ApiResponse(responseCode = "400", description = "La cantidad de tokens no es válida.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe el usuario o el jugador indicado.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/orders/sell")
    public ResponseEntity<AnuncioVentaResponse> sell(@RequestBody VenderTokensRequest request) {
        UUID sellerId = currentUserId();

        AnuncioVenta anuncio = mercadoService.vender(sellerId, request.jugadorId(), request.cantidad());

        AnuncioVentaResponse response = AnuncioVentaResponse.from(anuncio);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(summary = "Comprar tokens de un anuncio", description = "Compra tokens disponibles en un anuncio del marketplace.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compra completada correctamente."),
            @ApiResponse(responseCode = "400", description = "La solicitud o la cantidad de tokens no es válida.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El comprador no puede comprar su propio anuncio.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe el anuncio indicado.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El anuncio ya no está pendiente.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "El comprador no tiene créditos suficientes.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/orders/buy")
    public ResponseEntity<ComprarTokensResponse> buy(@Valid @RequestBody ComprarTokensRequest request) {
        UUID vendedorId = currentUserId();
        mercadoService.comprar(vendedorId, request.anuncioId(), request.cantidad());

        return ResponseEntity.ok(ComprarTokensResponse.from(true, "Compra completada correctamente", request.anuncioId()));
    }

    @Operation(summary = "Cancelar anuncio de venta", description = "Cancela un anuncio propio y devuelve los tokens al inventario del vendedor.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Anuncio cancelado correctamente."),
            @ApiResponse(responseCode = "403", description = "El usuario no es el vendedor del anuncio.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe el anuncio indicado.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El anuncio ya no está pendiente.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/orders/{anuncioId}")
    public ResponseEntity<CancelarVentaResponse> cancelSell(@PathVariable UUID anuncioId) {
        UUID vendedorId = currentUserId();
        mercadoService.cancelarVenta(vendedorId, anuncioId);

        return ResponseEntity.ok(CancelarVentaResponse.from(true, "Anuncio cancelado correctamente"));
    }

    @Operation(summary = "Listar transacciones de un usuario", description = "Retorna las operaciones de compra y venta de un usuario de manera paginada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transacciones consultadas correctamente.")
    })
    @GetMapping("/users/{id}/transactions")
    public ResponseEntity<PageResponse<OperacionResponse>> getTransactions(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<OperacionResponse> operaciones = mercadoService
                .getTransaccionesById(id, page, size)
                .map(OperacionResponse::from);

        return ResponseEntity.ok(PageResponse.from(operaciones));
    }

    @Operation(summary = "Comprar tokens al usuario administrador", description = "Compra tokens de un jugador directamente a la bolsa disponible del sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compra completada correctamente."),
            @ApiResponse(responseCode = "400", description = "La cantidad solicitada no es válida.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe el usuario o el jugador indicado.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "El usuario no tiene créditos suficientes.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/orders/super/buy")
    public ResponseEntity<ComprarTokensSuperResponse> comprarSuper(@RequestBody ComprarTokensSuperRequest request) {
        UUID buyerId = currentUserId();
        mercadoService.comprarSuper(buyerId, request.jugadorId(), request.cantidad());

        return ResponseEntity.ok(ComprarTokensSuperResponse.from(true, "Compra completada correctamente", request.jugadorId(), request.cantidad()));
    }

    @Operation(summary = "Consultar inventario de un usuario", description = "Retorna el inventario de tokens de un usuario de manera paginada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inventario consultado correctamente."),
            @ApiResponse(responseCode = "404", description = "No existe el usuario indicado.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/users/{id}/inventario")
    public ResponseEntity<PageResponse<InventarioUsuarioResponse>> getInventario(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<InventarioUsuarioResponse> inventario = mercadoService
                .getInventarioById(id, page, size)
                .map(InventarioUsuarioResponse::from);

        return ResponseEntity.ok(PageResponse.from(inventario));
    }

    private UUID currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UUID) authentication.getPrincipal();
    }

    @Operation(summary = "Consultar resumen de tokens", description = "Retorna el resumen de tokens del usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumen de tokens obtenido correctamente."),
            @ApiResponse(responseCode = "404", description = "No existe el inventario del usuario autenticado.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/resumen/tokens")
    public ResponseEntity<ResumenInventarioTokensResponse> obtenerResumenTokens() {
        UUID usuarioId = currentUserId();

        return ResponseEntity.ok(
                mercadoService.obtenerResumenTokens(usuarioId)
        );
    }

    @Operation(summary = "Consultar resumen de dinero", description = "Retorna el resumen de créditos y valoraciones monetarias del usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumen de dinero obtenido correctamente."),
            @ApiResponse(responseCode = "404", description = "No existe el inventario del usuario autenticado.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/resumen/dinero")
    public ResponseEntity<ResumenInventarioDineroResponse> obtenerResumenDinero() {
        UUID usuarioId = currentUserId();

        return ResponseEntity.ok(
                mercadoService.obtenerResumenDinero(usuarioId)
        );
    }
}
