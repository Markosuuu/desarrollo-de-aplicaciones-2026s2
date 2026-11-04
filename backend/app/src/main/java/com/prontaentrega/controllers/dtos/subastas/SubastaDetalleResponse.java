package com.prontaentrega.controllers.dtos.subastas;

import com.prontaentrega.models.Subasta;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SubastaDetalleResponse(
        UUID id,
        UUID propietarioId,
        UUID jugadorId,
        Integer cantidadOfrecida,
        BigDecimal ofertaActual,
        List<OfertaResponse> ofertasPrevias,
        String estado,
        LocalDateTime vencimiento) {

    public static SubastaDetalleResponse from(Subasta subasta, List<OfertaResponse> ofertas) {
        return new SubastaDetalleResponse(
                subasta.getId(),
                subasta.getPropietarioId(),
                subasta.getJugadorId(),
                subasta.getCantidadOfrecida(),
                subasta.getOfertaActual(),
                ofertas,
                subasta.getEstado().name(),
                subasta.getVencimiento());
    }
}
