package com.prontaentrega.controllers;

import com.prontaentrega.controllers.dtos.PageResponse;
import com.prontaentrega.controllers.dtos.transacciones.*;
import com.prontaentrega.models.AnuncioVenta;
import com.prontaentrega.models.InventarioUsuario;
import com.prontaentrega.models.Operacion;
import com.prontaentrega.services.MercadoService;

import jakarta.validation.Valid;
import java.util.*;

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

    @GetMapping("/marketplace/listings")
    public ResponseEntity<PageResponse<AnuncioVentaResponse>> getAnuncios(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AnuncioVentaResponse> anuncios = mercadoService
                .getAnuncios(page, size)
                .map(AnuncioVentaResponse::from);

        return ResponseEntity.ok(PageResponse.from(anuncios));
    }

    @PostMapping("/orders/sell")
    public ResponseEntity<AnuncioVentaResponse> sell(@RequestBody VenderTokensRequest request) {
        UUID sellerId = currentUserId();

        AnuncioVenta anuncio = mercadoService.vender(sellerId, request.jugadorId(), request.cantidad());

        AnuncioVentaResponse response = AnuncioVentaResponse.from(anuncio);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/orders/buy")
    public ResponseEntity<ComprarTokensResponse> buy(@Valid @RequestBody ComprarTokensRequest request) {
        UUID vendedorId = currentUserId();
        mercadoService.comprar(vendedorId, request.anuncioId(), request.cantidad());

        return ResponseEntity.ok(ComprarTokensResponse.from(true, "Compra completada correctamente", request.anuncioId()));
    }

    @DeleteMapping("/orders/{anuncioId}")
    public ResponseEntity<CancelarVentaResponse> cancelSell(@PathVariable UUID anuncioId) {
        UUID vendedorId = currentUserId();
        mercadoService.cancelarVenta(vendedorId, anuncioId);

        return ResponseEntity.ok(CancelarVentaResponse.from(true, "Anuncio cancelado correctamente"));
    }

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

    @PostMapping("/orders/super/buy")
    public ResponseEntity<ComprarTokensSuperResponse> comprarSuper(@RequestBody ComprarTokensSuperRequest request) {
        UUID buyerId = currentUserId();
        mercadoService.comprarSuper(buyerId, request.jugadorId(), request.cantidad());

        return ResponseEntity.ok(ComprarTokensSuperResponse.from(true, "Compra completada correctamente", request.jugadorId(), request.cantidad()));
    }

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
}
