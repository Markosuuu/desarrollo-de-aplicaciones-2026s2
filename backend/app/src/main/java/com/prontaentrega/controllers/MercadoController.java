package com.prontaentrega.controllers;

import com.prontaentrega.models.AnuncioVenta;
import com.prontaentrega.models.Operacion;
import com.prontaentrega.services.MercadoService;
import com.prontaentrega.controllers.dtos.transacciones.ComprarTokensRequest;
import com.prontaentrega.controllers.dtos.transacciones.AnuncioVentaResponse;
import com.prontaentrega.controllers.dtos.transacciones.OperacionResponse;
import com.prontaentrega.controllers.dtos.transacciones.VenderTokensRequest;

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
    public ResponseEntity<Map<String, Object>> buy(@RequestBody ComprarTokensRequest request) {
        UUID vendedorId = currentUserId();
        mercadoService.comprar(vendedorId, request.anuncioId());

        // TODO: Mejorar response
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Compra completada correctamente",
                "anuncioId", request.anuncioId()
        ));
    }

    @DeleteMapping("/orders/{anuncioId}")
    public ResponseEntity<Map<String, Object>> cancelSell(@PathVariable UUID anuncioId) {
        UUID vendedorId = currentUserId();
        mercadoService.cancelarVenta(vendedorId, anuncioId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Anuncio cancelado correctamente"
        ));
    }

    @GetMapping("/users/{id}/transactions")
    public ResponseEntity<List<OperacionResponse>> getTransactions(@PathVariable UUID id) {

        List<Operacion> operaciones = mercadoService.getTransaccionesById(id);

        List<OperacionResponse> response = operaciones.stream()
                .map(OperacionResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    private UUID currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UUID) authentication.getPrincipal();
    }

    @PostMapping("/orders/super/buy")
    public ResponseEntity<Map<String, Object>> comprarSuper(@RequestBody ComprarTokensSuperRequest request) {
        UUID buyerId = currentUserId();
        mercadoService.comprarSuper(buyerId, request.jugadorId(), request.cantidad());

        // TODO: Mejorar response
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Compra completada correctamente",
                "jugadorId", request.jugadorId(),
                "cantidad", request.cantidad()
        ));
    }

    public record ComprarTokensSuperRequest(UUID jugadorId, Integer cantidad) {}
}
