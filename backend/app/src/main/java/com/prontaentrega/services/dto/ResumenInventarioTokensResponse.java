package com.prontaentrega.services.dto;
import java.util.List;

public record ResumenInventarioTokensResponse(
        Long total,
        List<JugadorResumenTokensInventario> jugadores
) {
}