package com.prontaentrega.controllers.dtos.transacciones;

import com.prontaentrega.models.Operacion;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OperacionResponse(
        UUID id,
        String tipo,
        String estado,
        String nombreJugador,
        Integer cantidad,
        BigDecimal precioPorToken,
        BigDecimal total,
        UUID usuarioId,
        UUID contraparteId,
        String usuarioNombre,
        String contraparteNombre,
        LocalDateTime creadaEn
) {
    public static OperacionResponse from(Operacion operacion) {
        return new OperacionResponse(
                operacion.getId(),
                operacion.getTipo().name(),
                operacion.getEstado().name(),
                operacion.getJugador().getNombre(),
                operacion.getCantidad(),
                operacion.getPrecioPorToken(),
                operacion.getTotal(),
                operacion.getUsuario().getId(),
                operacion.getContraparte().getId(),
                operacion.getUsuario().getNombre(),
                operacion.getContraparte().getNombre(),
                operacion.getCreadaEn()
        );
    }
}
