package com.prontaentrega.models.cotizacion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistorialCotizacion {
    private UUID id;
    private UUID bolsaId;
    private BigDecimal precio;
    private LocalDateTime fecha;

    public HistorialCotizacion(UUID boldaId, BigDecimal precio, LocalDateTime fecha) {
        this.bolsaId = boldaId;
        this.precio = precio;
        this.fecha = fecha;
    }
}