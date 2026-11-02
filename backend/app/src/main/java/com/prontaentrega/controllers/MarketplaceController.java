package com.prontaentrega.controllers;

import com.prontaentrega.services.MarketplaceService;
import com.prontaentrega.services.dto.ComprarTokensRequest;
import com.prontaentrega.services.dto.MarketplaceListingResponse;
import com.prontaentrega.services.dto.OperacionResponse;
import com.prontaentrega.services.dto.VenderTokensRequest;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
public class MarketplaceController {
    private final MarketplaceService marketplaceService;

    public MarketplaceController(MarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    @GetMapping("/marketplace/listings")
    public ResponseEntity<List<MarketplaceListingResponse>> getListings(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "32") int size) {
        return ResponseEntity.ok(marketplaceService.getListings(page, size));
    }

    @PostMapping("/orders/sell")
    public ResponseEntity<MarketplaceListingResponse> sell(@RequestBody VenderTokensRequest request) {
        UUID sellerId = currentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(marketplaceService.sellTokens(sellerId, request.jugadorId(), request.cantidad()));
    }

    @PostMapping("/orders/buy")
    public ResponseEntity<Map<String, Object>> buy(@RequestBody ComprarTokensRequest request) {
        UUID buyerId = currentUserId();
        marketplaceService.buyTokens(buyerId, request.anuncioId());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Compra completada correctamente",
                "anuncioId", request.anuncioId()
        ));
    }

    @GetMapping("/users/{id}/transactions")
    public ResponseEntity<List<OperacionResponse>> getTransactions(@PathVariable UUID id) {
        return ResponseEntity.ok(marketplaceService.getUserTransactions(id));
    }

    private UUID currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UUID) authentication.getPrincipal();
    }
}
