package com.prontaentrega.controllers.dtos.subastas;

import com.prontaentrega.controllers.dtos.auth.UsuarioResponse;
import com.prontaentrega.models.Oferta;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OfertaResponse(
        UUID id,
        UUID subastaId,
        UsuarioResponse usuario,
        BigDecimal puja,
        LocalDateTime timestamp,
        Boolean estado) {

    public static OfertaResponse from(Oferta oferta) {
        return new OfertaResponse(
                oferta.getId(),
                oferta.getSubastaId(),
                UsuarioResponse.from(oferta.getUsuario()),
                oferta.getPuja(),
                oferta.getCreadoEn(),
                oferta.getEsSuperior());
    }
}
