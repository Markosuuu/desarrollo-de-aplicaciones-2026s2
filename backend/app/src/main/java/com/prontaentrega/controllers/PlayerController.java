package com.prontaentrega.controllers;

import com.prontaentrega.controllers.dtos.PlayerResponse;
import com.prontaentrega.services.PlayerCatalogService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PlayerController {
    private final PlayerCatalogService playerCatalogService;

    public PlayerController(PlayerCatalogService playerCatalogService) {
        this.playerCatalogService = playerCatalogService;
    }

    @GetMapping("/players")
    public ResponseEntity<?> getPlayers(
            @RequestParam(required = false) String liga,
            @RequestParam(required = false) String equipo,
            @RequestParam(required = false) String nombre,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int perPage) {
        var response = playerCatalogService.search(liga, equipo, nombre, page, perPage);
        return ResponseEntity.ok(response);
    }
}
