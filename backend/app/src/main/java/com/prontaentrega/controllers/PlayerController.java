package com.prontaentrega.controllers;

import com.prontaentrega.services.dto.RefreshCatalogResponse;
import com.prontaentrega.services.PlayerCatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller HTTP del catalogo de jugadores.
 */
@RestController
@RequestMapping("/api")
public class PlayerController {
    private final PlayerCatalogService playerCatalogService;

    /**
     * Recibe el servicio de catalogo usado por los endpoints de jugadores.
     */
    public PlayerController(PlayerCatalogService playerCatalogService) {
        this.playerCatalogService = playerCatalogService;
    }

    /**
     * Lista jugadores persistidos localmente con filtros opcionales.
     */
    @GetMapping("/players")
    public ResponseEntity<?> getPlayers(
            @RequestParam(required = false) String liga,
            @RequestParam(required = false) String equipo,
            @RequestParam(required = false) String nombre,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int perPage) {

        // MARCOS, cuando hagas esto, no se como lo vas a hacer, pero cuando lo hagas, capaz estaria bueno
        // que el repository te los devuelva ordenados por rating???? Manejalo vos, yo solo lo sugiero xD
        var response = playerCatalogService.search(liga, equipo, nombre, page, perPage);
        return ResponseEntity.ok(response);
    }

    /**
     * Dispara la actualizacion publica del catalogo desde WhoScored.
     */
    @PostMapping("/players/update")
    public ResponseEntity<RefreshCatalogResponse> updatePlayers() {
        return ResponseEntity.status(HttpStatus.OK).body(playerCatalogService.refreshFromWhoScored());
    }
}
