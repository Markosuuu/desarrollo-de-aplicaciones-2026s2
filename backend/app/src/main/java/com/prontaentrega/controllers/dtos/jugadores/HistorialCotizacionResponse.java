package com.prontaentrega.controllers.dtos.jugadores;

import com.prontaentrega.models.HistorialCotizacion;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class HistorialCotizacionResponse {
    private LocalDateTime fecha;
    private BigDecimal precio;

    public static HistorialCotizacionResponse from(HistorialCotizacion historial) {
        return new HistorialCotizacionResponse(
                historial.getFecha(),
                historial.getPrecio()
        );
    }
}
