package com.prontaentrega.controllers.dtos.subastas;

import com.prontaentrega.models.Subasta;
import com.prontaentrega.models.enums.EstadoOferta;
import java.math.BigDecimal;
import java.util.UUID;

public record CerrarSubastaResponse(
        UUID id,
        Boolean estado,
        UUID ganadorId,
        BigDecimal montoFinal,
        String resultado) {

    public static CerrarSubastaResponse from(Subasta subasta) {
        boolean huboVenta = subasta.getOfertaActual() != null
                && subasta.getOfertaActual().getEsSuperior();
        return new CerrarSubastaResponse(
                subasta.getId(),
                subasta.getActiva(),
                huboVenta ? subasta.getOfertaActual().getUsuarioId() : null,
                huboVenta ? subasta.getOfertaActual().getPuja() : null,
                huboVenta ? "VENTA" : "RETORNO_TOKENS");
    }
}
