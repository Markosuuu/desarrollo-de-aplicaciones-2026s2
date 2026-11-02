package com.prontaentrega.services.dto;

import com.prontaentrega.models.AnuncioVenta;
import java.math.BigDecimal;
import java.util.UUID;

public record MarketplaceListingResponse(
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
    public static MarketplaceListingResponse from(AnuncioVenta anuncio, String vendedorNombre, String nombreJugador) {
        return new MarketplaceListingResponse(
                anuncio.getId(),
                anuncio.getVendedorId(),
                vendedorNombre,
                anuncio.getJugadorId(),
                nombreJugador,
                anuncio.getCantidadTokens(),
                anuncio.getPrecioPorToken(),
                anuncio.getPrecioTotal(),
                anuncio.getEstado().name()
        );
    }
}
