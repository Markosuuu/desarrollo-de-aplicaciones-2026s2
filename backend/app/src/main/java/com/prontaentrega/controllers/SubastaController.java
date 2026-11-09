package com.prontaentrega.controllers;

import com.prontaentrega.controllers.dtos.PageResponse;
import com.prontaentrega.controllers.dtos.subastas.*;
import com.prontaentrega.models.Oferta;
import com.prontaentrega.models.Subasta;
import com.prontaentrega.services.SubastaService;
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

    @GetMapping
    public ResponseEntity<PageResponse<SubastaResponse>> listarSubastas(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SubastaResponse> subastas = subastaService
                .listarSubastas(page, size)
                .map(SubastaResponse::from);

        return ResponseEntity.ok(PageResponse.from(subastas));
    }

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

    @GetMapping("/{id}")
    public ResponseEntity<SubastaDetalleResponse> obtenerSubasta(@PathVariable UUID id) {
        Subasta subasta = subastaService.obtenerSubasta(id);
        List<OfertaResponse> ofertas = subastaService.obtenerOfertas(id).stream()
                .map(OfertaResponse::from)
                .toList();
        return ResponseEntity.ok(SubastaDetalleResponse.from(subasta, ofertas));
    }

    @PostMapping("/ofertar")
    public ResponseEntity<OfertaResponse> ofertar(@Valid @RequestBody OfertarSubastaRequest request) {
        UUID usuarioId = currentUserId();
        Oferta oferta = subastaService.ofertar(usuarioId, request.subastaId(), request.puja(), request.idempotencyKey());
        return ResponseEntity.ok(OfertaResponse.from(oferta));
    }

    @PatchMapping("/{id}/cerrar")
    public ResponseEntity<CerrarSubastaResponse> cerrarSubasta(
            @PathVariable UUID id,
            @RequestBody(required = false) CerrarSubastaRequest request
    ) {
        UUID usuarioId = currentUserId();
        Subasta subasta = subastaService.cerrarSubasta(usuarioId, id, request.motivo());
        return ResponseEntity.ok(CerrarSubastaResponse.from(subasta));
    }

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
