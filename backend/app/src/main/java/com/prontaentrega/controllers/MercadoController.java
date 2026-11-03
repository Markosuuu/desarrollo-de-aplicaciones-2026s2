package com.prontaentrega.controllers;

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
    public ResponseEntity<List<AnuncioVentaResponse>> getAnuncios(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "32") int size) {
        Page<AnuncioVenta> anuncios = mercadoService.getAnuncios(page, size);

        return ResponseEntity.ok(anuncios.getContent().stream()
                .map(AnuncioVentaResponse::from)
                .toList());
    }

    @PostMapping("/orders/sell")
    public ResponseEntity<AnuncioVentaResponse> sell(@RequestBody VenderTokensRequest request) {
        UUID sellerId = currentUserId();

        AnuncioVenta anuncio = mercadoService.vender(sellerId, request.jugadorId(), request.cantidad());

        AnuncioVentaResponse response = AnuncioVentaResponse.from(anuncio);;

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
    public ResponseEntity<List<OperacionResponse>> getTransactions(@PathVariable UUID id) {

        List<Operacion> operaciones = mercadoService.getTransaccionesById(id);

        List<OperacionResponse> response = operaciones.stream()
                .map(OperacionResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/orders/super/buy")
    public ResponseEntity<ComprarTokensSuperResponse> comprarSuper(@RequestBody ComprarTokensSuperRequest request) {
        UUID buyerId = currentUserId();
        mercadoService.comprarSuper(buyerId, request.jugadorId(), request.cantidad());

        return ResponseEntity.ok(ComprarTokensSuperResponse.from(true, "Compra completada correctamente", request.jugadorId(), request.cantidad()));
    }

    @GetMapping("/users/{id}/inventario")
    public ResponseEntity<List<InventarioUsuarioResponse>> getInventario(@PathVariable UUID id) {

        List<InventarioUsuario> inventario = mercadoService.getInventarioById(id);

        List<InventarioUsuarioResponse> response = inventario.stream()
                .map(InventarioUsuarioResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    private UUID currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UUID) authentication.getPrincipal();
    }
}
