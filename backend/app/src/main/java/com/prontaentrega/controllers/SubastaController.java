package com.prontaentrega.controllers;

import com.prontaentrega.controllers.dtos.PageResponse;
import com.prontaentrega.controllers.dtos.subastas.*;
import com.prontaentrega.controllers.exceptions.ErrorResponse;
import com.prontaentrega.models.subastas.Oferta;
import com.prontaentrega.models.subastas.Subasta;
import com.prontaentrega.services.SubastaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subastas")
public class SubastaController {
    private final SubastaService subastaService;

    public SubastaController(SubastaService subastaService) {
        this.subastaService = subastaService;
    }

    @Operation(summary = "Publicar una subasta", description = "Publica una subasta con una fecha de finalizacion.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201", description = "Retorna la subasta creada correctamente."
            ),
            @ApiResponse(responseCode = "400", description = "Retorna error en caso de no cumplirse las condiciones" +
                    " de los campos de la request para crear una subasta.", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            ))
    })
    @PostMapping
    public ResponseEntity<SubastaResponse> crearSubasta(@Valid @RequestBody CrearSubastaRequest request) {
        UUID usuarioId = currentUserId();
        Subasta subasta = subastaService.crearSubasta(
                usuarioId,
                request.jugadorId(),
                request.cantidad(),
                request.precioInicial(),
                request.vencimiento());
        return ResponseEntity.status(HttpStatus.CREATED).body(SubastaResponse.from(subasta));
    }

    @Operation(summary = "Listar subastas", description = "Lista las subastas activas de manera paginada.")
    @ApiResponse(
            responseCode = "200", description = "Retorna la lista de subastas activas, con informacion respecto al paginado."
    )
    @GetMapping
    public ResponseEntity<PageResponse<SubastaResponse>> listarSubastas(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SubastaResponse> subastas = subastaService
                .listarSubastas(page, size)
                .map(SubastaResponse::from);

        return ResponseEntity.ok(PageResponse.from(subastas));
    }

    @Operation(summary = "Listar subastas filtradas", description = "Lista las subastas activas " +
            "de manera paginada aplicando filtros en base al nombre del jugador que se oferta en la subasta.")
    @ApiResponse(responseCode = "200", description = "Retorna las subastas que coinciden con los filtros y la información de paginado.")
    @PostMapping("/buscarPorNombre")
    public ResponseEntity<PageResponse<SubastaResponse>> listarSubastasPorNombre(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestBody SubastaRequest request
    ) {
        Page<SubastaResponse> subastas;
        if (!StringUtils.hasText(request.jugador()) && !StringUtils.hasText(request.user())) {
            subastas = subastaService
                    .listarSubastas(page, size)
                    .map(SubastaResponse::from);
        } else {
            subastas = subastaService
                    .listarSubastasPorNombre(page, size, request.user(), request.jugador())
                    .map(SubastaResponse::from);
        }

        return ResponseEntity.ok(PageResponse.from(subastas));
    }

    public record SubastaRequest(String user, String jugador) {}

    @Operation(summary = "Retornar una subasta por ID", description = "Retorna la subasta que tenga el id" +
            "correspondiente. En caso de no existir subasta, arroja un codigo 404")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retorna la subasta que corresponde con el id " +
                    "dado por parametro."),
            @ApiResponse(responseCode = "404", description = "Retorna error en caso de no encontrar una" +
                    "subasta con el id dado por parametro.", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            ))
    })
    @GetMapping("/{id}")
    public ResponseEntity<SubastaDetalleResponse> obtenerSubasta(@PathVariable UUID id) {
        Subasta subasta = subastaService.obtenerSubasta(id);
        List<OfertaResponse> ofertas = subastaService.obtenerOfertas(id).stream()
                .map(OfertaResponse::from)
                .toList();
        return ResponseEntity.ok(SubastaDetalleResponse.from(subasta, ofertas));
    }

    @Operation(summary = "Ofertar en una subasta", description = "Dejar una puja en una subasta para poder " +
            "ganar.")
    @PostMapping("/ofertar")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retorna la informacion de la oferta creada."),
            @ApiResponse(responseCode = "404", description = "Retorna error en caso de no encontrar una subasta",
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "400", description = "Retorna error en caso de no realizar una oferta valida",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "409", description = "La subasta no está activa, la clave de idempotencia entra en conflicto o la oferta ya fue registrada.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
            ))
    })
    public ResponseEntity<OfertaResponse> ofertar(@Valid @RequestBody OfertarSubastaRequest request) {
        UUID usuarioId = currentUserId();
        Oferta oferta = subastaService.ofertar(usuarioId, request.subastaId(), request.puja(), request.idempotencyKey());
        return ResponseEntity.ok(OfertaResponse.from(oferta));
    }

    @Operation(summary = "Cerrar una subasta por ID", description = "Cierra una subasta con el id dado." +
            " En caso de no existir subasta, arroja un codigo 404, y en caso de que se cierre una subasta" +
            " ya cerrada, arroja codigo 409.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retorna la subasta que corresponde con el id " +
                    "dado por parametro, cerrada y con el id del usuario ganador si es que existe, y la " +
                    "puja mas alta."),
            @ApiResponse(responseCode = "404", description = "Retorna error en caso de no encontrar una" +
                    "subasta con el id dado por parametro.", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )),
            @ApiResponse(responseCode = "409", description = "Retorna error en caso de volver a cerrar una" +
                    "subasta ya cerrada", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            ))
    })
    @PatchMapping("/{id}/cerrar")
    public ResponseEntity<CerrarSubastaResponse> cerrarSubasta(
            @PathVariable UUID id
    ) {
        UUID usuarioId = currentUserId();
        Subasta subasta = subastaService.cerrarSubasta(usuarioId, id);
        return ResponseEntity.ok(CerrarSubastaResponse.from(subasta));
    }


    @Operation(summary = "Retornar ofertas de una subasta por ID", description = "Retorna las ofertas de una subasta que tenga el id" +
            "correspondiente. En caso de no existir subasta, arroja un codigo 404")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retorna las ofertas de subasta que correspondab con " +
                    "el id de subasta dado por parametro. En caso de no hallar ofertas, o no hayar una subasta con " +
                    "dicho id, retorna una lista de contenido vacio")
    })
    @GetMapping("/{id}/ofertas")
    public ResponseEntity<List<OfertaResponse>> obtenerOfertas(@PathVariable UUID id) {
        List<Oferta> ofertas = subastaService.obtenerOfertas(id);

        List<OfertaResponse> response = ofertas.stream().map(OfertaResponse::from).toList();

        return ResponseEntity.ok(response);
    }

    private UUID currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UUID) authentication.getPrincipal();
    }
}
