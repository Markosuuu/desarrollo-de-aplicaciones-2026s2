package com.prontaentrega.models;

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
}