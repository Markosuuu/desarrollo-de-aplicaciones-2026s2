package com.prontaentrega.controllers.dtos.subastas;

import com.prontaentrega.models.Subasta;
import java.math.BigDecimal;
import java.util.UUID;

public record CerrarSubastaResponse(
        UUID id,
        String estado,
        UUID ganadorId,
        BigDecimal montoFinal,
        String resultado) {

    public static CerrarSubastaResponse from(Subasta subasta) {
        boolean huboVenta = subasta.getOfertanteActual() != null;
        return new CerrarSubastaResponse(
                subasta.getId(),
                subasta.getEstado().name(),

                // TODO: check this
                huboVenta ? subasta.getOfertanteActual().getId() : null,
                huboVenta ? subasta.getOfertaActual() : null,
                huboVenta ? "VENTA" : "RETORNO_TOKENS");
    }
}
