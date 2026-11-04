package com.prontaentrega.controllers.dtos.subastas;

import com.prontaentrega.models.Subasta;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SubastaResponse(
        UUID id,
        UUID propietarioId,
        UUID jugadorId,
        Integer cantidadOfrecida,
        BigDecimal ofertaActual,
        String estado,
        LocalDateTime vencimiento) {

    public static SubastaResponse from(Subasta subasta) {
        return new SubastaResponse(
                subasta.getId(),
                subasta.getPropietarioId(),
                subasta.getJugadorId(),
                subasta.getCantidadOfrecida(),
                subasta.getOfertaActual() == null ? null : subasta.getOfertaActual().getPuja(),
                subasta.getEstado().name(),
                subasta.getVencimiento());
    }
}
