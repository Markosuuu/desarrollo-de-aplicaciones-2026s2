package com.prontaentrega.controllers.dtos.subastas;

import com.prontaentrega.controllers.dtos.auth.UsuarioResponse;
import com.prontaentrega.controllers.dtos.jugadores.PlayerResponse;
import com.prontaentrega.models.Subasta;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SubastaResponse(
        UUID id,
        UsuarioResponse propietario,
        PlayerResponse jugador,
        Integer cantidadOfrecida,
        BigDecimal ofertaActual,
        Boolean activa,
        LocalDateTime vencimiento) {

    public static SubastaResponse from(Subasta subasta) {
        return new SubastaResponse(
                subasta.getId(),
                UsuarioResponse.from(subasta.getPropietario()),
                PlayerResponse.from(subasta.getBolsaDeJugador()),
                subasta.getCantidadOfrecida(),
                subasta.getOfertaActual() == null ? null : subasta.getOfertaActual().getPuja(),
                subasta.getActiva(),
                subasta.getVencimiento());
    }
}
