package com.prontaentrega.controllers.dtos.transacciones;

import com.prontaentrega.controllers.dtos.auth.UsuarioResponse;
import com.prontaentrega.controllers.dtos.jugadores.PlayerResponse;
import com.prontaentrega.models.InventarioUsuario;

import java.util.UUID;

public record InventarioUsuarioResponse(
        UUID id,
        UsuarioResponse usuario,
        PlayerResponse jugador,
        Integer tokensDisponibles,
        Integer tokensEnVenta,
        String actualizadoEn
) {
    public static InventarioUsuarioResponse from(InventarioUsuario inventarioUsuario) {
        return new InventarioUsuarioResponse(
                inventarioUsuario.getId(),
                UsuarioResponse.from(inventarioUsuario.getUsuario()),
                PlayerResponse.from(inventarioUsuario.getBolsaDeJugador()),
                inventarioUsuario.getTokensDisponibles(),
                inventarioUsuario.getTokensEnVenta(),
                inventarioUsuario.getActualizadoEn().toString()
        );
    }
}
