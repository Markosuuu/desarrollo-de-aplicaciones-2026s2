package com.prontaentrega.controllers;

import com.prontaentrega.controllers.dtos.jugadores.CatalogResponse;
import com.prontaentrega.controllers.dtos.jugadores.HistorialCotizacionResponse;
import com.prontaentrega.controllers.dtos.jugadores.PlayerResponse;
import com.prontaentrega.models.BolsaDeJugador;
import com.prontaentrega.models.enums.Liga;
import com.prontaentrega.services.HistorialCotizacionService;
import com.prontaentrega.services.dto.RefreshCatalogResponse;
import com.prontaentrega.services.PlayerCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller HTTP del catalogo de jugadores.
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class PlayerController {
    private final PlayerCatalogService playerCatalogService;
    private final HistorialCotizacionService historialService;
    /**
     * Recibe el servicio de catalogo usado por los endpoints de jugadores.
     */
    public PlayerController(PlayerCatalogService playerCatalogService, HistorialCotizacionService historialService) {
        this.playerCatalogService = playerCatalogService;
        this.historialService = historialService;
    }

    /**
     * Lista jugadores persistidos localmente con filtros opcionales.
     */
    @Operation(
            summary = "Listar jugadores",
            description = "Lista los jugadores de manera paginada con filtros opcionales de nomrbe, equipo y liga."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retorna una pagina, con sus jugadores segun el filtro aplicado, e información referente al paginado.")
    })
    @GetMapping("/players")
    public ResponseEntity<CatalogResponse> getPlayers(
            @RequestParam(required = false) Liga liga,
            @RequestParam(required = false) String equipo,
            @RequestParam(required = false) String nombre,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int perPage) {

        log.info("⌛ Buscando los jugadores");
        Page<BolsaDeJugador> jugadores = playerCatalogService.search(liga, equipo, nombre, page, perPage);

        List<PlayerResponse> players = jugadores.getContent().stream()
            .map(PlayerResponse::from)
            .toList();

        var response = new CatalogResponse(players, new CatalogResponse.Paginacion(
                page,
                perPage,
                jugadores.getTotalElements(),
                jugadores.getTotalPages()
        ));

        log.info("✅ Jugadores encontrados: {}", jugadores.getTotalElements());
        return ResponseEntity.ok(response);
    }

    /**
     * Dispara la actualizacion pública del catalogo desde WhoScored.
     */
    @Operation(
            summary = "Actualizar jugadores manualmente",
            description = "Actualiza los jugadores de la base de datos por medio de scrapping de la pagina WhoScored, indicando cuantos nuevos se crearon, actualizaron o ignoraron."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizacion exitosa y completada correctamente."),
            @ApiResponse(responseCode = "502", description = "No se pudo completar la actualizacion debido a que WhoScored entrego datos no utilizables o ocurrio un error inesperado. No se realizan cambios y se conserva la informacion")
    })
    @PostMapping("/players/update")
    public ResponseEntity<RefreshCatalogResponse> updatePlayers() {
        log.info("⌛ Actualizando catalogo de jugadores desde WhoScored");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(playerCatalogService.refreshFromWhoScored());
    }

    @GetMapping("/players/{id}")
    public ResponseEntity<PlayerResponse> searchById(@PathVariable UUID id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(PlayerResponse.from(playerCatalogService.searchById(id)));
    }

    @GetMapping("/players/{id}/quotes")
    public ResponseEntity<List<HistorialCotizacionResponse>> buscarHistorial(
            @PathVariable UUID id
    ) {
        List<HistorialCotizacionResponse> response =
                historialService.buscarHistorial(id)
                        .stream()
                        .map(HistorialCotizacionResponse::from)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/players/ranking")
    public ResponseEntity<CatalogResponse> getRanking(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "20") int perPage) {
        Page<BolsaDeJugador> jugadores = playerCatalogService.getRanking(page, perPage);

        List<PlayerResponse> players = jugadores.getContent().stream()
                .map(PlayerResponse::from)
                .toList();

        var response = new CatalogResponse(players, new CatalogResponse.Paginacion(
                page,
                perPage,
                jugadores.getTotalElements(),
                jugadores.getTotalPages()
        ));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
