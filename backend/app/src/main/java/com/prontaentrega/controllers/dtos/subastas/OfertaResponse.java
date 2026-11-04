package com.prontaentrega.controllers.dtos.subastas;

import com.prontaentrega.models.Oferta;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OfertaResponse(
        UUID id,
        UUID subastaId,
        UUID usuarioId,
        BigDecimal puja,
        LocalDateTime timestamp,
        Boolean estado) {

    public static OfertaResponse from(Oferta oferta) {
        return new OfertaResponse(
                oferta.getId(),
                oferta.getSubastaId(),
                oferta.getUsuarioId(),
                oferta.getPuja(),
                oferta.getCreadoEn(),
                oferta.getEsSuperior());
    }
}
