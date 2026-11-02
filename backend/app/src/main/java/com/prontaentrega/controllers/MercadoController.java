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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        UUID buyerId = currentUserId();
        mercadoService.comprar(buyerId, request.anuncioId());

        // TODO: Mejorar response
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Compra completada correctamente",
                "anuncioId", request.anuncioId()
        ));
    }

    @GetMapping("/users/{id}/transactions")
    public ResponseEntity<List<OperacionResponse>> getTransactions(@PathVariable UUID id) {

        List<Operacion> operaciones = mercadoService.getTransaccionesById(id);

        List<OperacionResponse> response = operaciones.stream()
                .map(OperacionResponse::from)
                .toList();

        response.sort(Comparator.comparing(OperacionResponse::creadaEn).reversed());

        return ResponseEntity.ok(response);
    }

    private UUID currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UUID) authentication.getPrincipal();
    }
}
