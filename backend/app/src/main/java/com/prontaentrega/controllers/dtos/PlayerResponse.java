package com.prontaentrega.controllers.dtos;

import com.prontaentrega.models.Jugador;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PlayerResponse(UUID id, String nombre, String equipo, String liga,
                            BigDecimal cotizacion, LocalDateTime fechaActualizacion) {
    public static PlayerResponse from(Jugador jugador) {
        return new PlayerResponse(jugador.getId(), jugador.getNombre(), jugador.getEquipo(),
                jugador.getLiga(), jugador.getCotizacionActual(), jugador.getFechaActualizacion());
    }
}
