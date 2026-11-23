package com.prontaentrega.services.dto;
import java.util.List;

public record ResumenInventarioResponse(
        Long total,
        List<JugadorResumenInventario> jugadores
) {
}