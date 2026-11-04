package com.prontaentrega.controllers.dtos.transacciones;

import com.prontaentrega.models.AnuncioVenta;
import java.math.BigDecimal;
import java.util.UUID;

public record AnuncioVentaResponse(
        UUID id,
        UUID vendedorId,
        String vendedorNombre,
        UUID jugadorId,
        String nombreJugador,
        Integer cantidadTokens,
        BigDecimal precioPorToken,
        BigDecimal precioTotal,
        String estado
) {
    public static AnuncioVentaResponse from(AnuncioVenta anuncio) {
        return new AnuncioVentaResponse(
                anuncio.getId(),
                anuncio.getVendedor().getId(),
                anuncio.getVendedor().getNombre(),
                anuncio.getBolsaDeJugador().getId(),
                anuncio.getBolsaDeJugador().getEstadisticasDeportivas().getJugador().getNombre(),
                anuncio.getCantidadTokens(),
                anuncio.getPrecioPorToken(),
                anuncio.getPrecioTotal(),
                anuncio.getEstado().name()
        );
    }
}
