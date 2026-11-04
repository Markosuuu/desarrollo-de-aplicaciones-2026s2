package com.prontaentrega.controllers.dtos.subastas;

import com.prontaentrega.controllers.dtos.auth.UsuarioResponse;
import com.prontaentrega.controllers.dtos.jugadores.PlayerResponse;
import com.prontaentrega.models.Subasta;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SubastaDetalleResponse(
        UUID id,
        UsuarioResponse propietario,
        PlayerResponse jugador,
        Integer cantidadOfrecida,
        BigDecimal ofertaActual,
        List<OfertaResponse> ofertasPrevias,
        Boolean estado,
        LocalDateTime vencimiento) {

    public static SubastaDetalleResponse from(Subasta subasta, List<OfertaResponse> ofertas) {
        return new SubastaDetalleResponse(
                subasta.getId(),
                UsuarioResponse.from(subasta.getPropietario()),
                PlayerResponse.from(subasta.getBolsaDeJugador()),
                subasta.getCantidadOfrecida(),
                subasta.getOfertaActual() == null ? null : subasta.getOfertaActual().getPuja(),
                ofertas,
                subasta.getActiva(),
                subasta.getVencimiento());
    }
}
