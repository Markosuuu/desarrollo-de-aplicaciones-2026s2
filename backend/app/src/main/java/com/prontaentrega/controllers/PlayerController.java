package com.prontaentrega.controllers;

import com.prontaentrega.controllers.dtos.jugadores.CatalogResponse;
import com.prontaentrega.controllers.dtos.jugadores.HistorialCotizacionResponse;
import com.prontaentrega.controllers.dtos.jugadores.PlayerResponse;
import com.prontaentrega.models.jugadores.BolsaDeJugador;
import com.prontaentrega.models.jugadores.Liga;
import com.prontaentrega.services.CotizacionService;
import com.prontaentrega.services.dto.RefreshCatalogResponse;
import com.prontaentrega.services.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import com.prontaentrega.controllers.exceptions.ErrorResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PlayerController {
    private final PlayerService playerService;
    private final CotizacionService cotizacionService;

    /**
     * Recibe el servicio de catalogo usado por los endpoints de jugadores.
     */
    public PlayerController(PlayerService playerService, CotizacionService cotizacionService) {
        this.playerService = playerService;
        this.cotizacionService = cotizacionService;
    }

    /**
     * Lista jugadores persistidos localmente con filtros opcionales.
     */
    @Operation(
            summary = "Listar jugadores",
            description = "Lista los jugadores de manera paginada con filtros opcionales de nombre, equipo y liga."
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

        Page<BolsaDeJugador> jugadores = playerService.search(liga, equipo, nombre, page, perPage);

        List<PlayerResponse> players = jugadores.getContent().stream()
            .map(PlayerResponse::from)
            .toList();

        var response = new CatalogResponse(players, new CatalogResponse.Paginacion(
                page,
                perPage,
                jugadores.getTotalElements(),
                jugadores.getTotalPages()
        ));

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
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(playerService.refreshFromWhoScored());
    }

    @Operation(summary = "Buscar jugador por ID", description = "Retorna el jugador correspondiente al identificador indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Jugador encontrado."),
            @ApiResponse(responseCode = "404", description = "No existe un jugador con el identificador indicado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/players/{id}")
    public ResponseEntity<PlayerResponse> searchById(@PathVariable UUID id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(PlayerResponse.from(playerService.searchById(id)));
    }

    @Operation(summary = "Consultar historial de cotizaciones", description = "Retorna el historial completo de cotizaciones de un jugador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial de cotizaciones encontrado."),
            @ApiResponse(responseCode = "404", description = "El jugador o su historial no fueron encontrados.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/players/{id}/quotes")
    public ResponseEntity<List<HistorialCotizacionResponse>> buscarHistorial(
            @PathVariable UUID id
    ) {
        List<HistorialCotizacionResponse> response =
                cotizacionService.buscarHistorialById(id)
                        .stream()
                        .map(HistorialCotizacionResponse::from)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Consultar cotización por fecha", description = "Retorna la cotización de un jugador registrada en la fecha indicada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cotización encontrada para la fecha solicitada."),
            @ApiResponse(responseCode = "404", description = "No existe cotización para el jugador y la fecha indicados.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/players/{id}/quotes/{fecha}")
    public ResponseEntity<HistorialCotizacionResponse> buscarHistorialPorFecha(
            @PathVariable UUID id,
            @PathVariable LocalDate fecha
            ) {
        HistorialCotizacionResponse response = HistorialCotizacionResponse.from(
                cotizacionService.buscarHistorialPorFecha(id, fecha)
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar ranking de jugadores", description = "Retorna los jugadores ordenados por ranking de manera paginada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ranking consultado correctamente.")
    })
    @GetMapping("/players/ranking")
    public ResponseEntity<CatalogResponse> getRanking(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "20") int perPage) {
        Page<BolsaDeJugador> jugadores = playerService.getRanking(page, perPage);

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

    @Operation(summary = "Buscar ranking por nombre", description = "Filtra el ranking de jugadores por nombre y devuelve los resultados de manera paginada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ranking filtrado consultado correctamente.")
    })
    @PostMapping("/players/ranking")
    public ResponseEntity<CatalogResponse> getRankingByNombre(@RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "20") int perPage,
                                                            @RequestBody JugadorRequest request) {

        Page<BolsaDeJugador> jugadores;

        if (!StringUtils.hasText(request.nombre())) {
            jugadores = playerService.getRanking(page, perPage);
        } else {
            jugadores = playerService.getRankingByNombre(page, perPage, request.nombre());
        }

        List<PlayerResponse> jugadoresResponse = jugadores.getContent().stream()
                .map(PlayerResponse::from)
                .toList();

        var response = new CatalogResponse(jugadoresResponse, new CatalogResponse.Paginacion(
                page,
                perPage,
                jugadores.getTotalElements(),
                jugadores.getTotalPages()
        ));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    public record JugadorRequest(String nombre) {}
}
