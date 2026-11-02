package com.prontaentrega.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AnuncioVenta {
    private UUID id;
    private UUID vendedorId;
    private UUID jugadorId;
    private Integer cantidadTokens;
    private BigDecimal precioPorToken;
    private BigDecimal precioTotal;
    private EstadoAnuncioVenta estado;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    public AnuncioVenta(UUID vendedorId, UUID jugadorId, Integer cantidadTokens,
                       BigDecimal precioPorToken, BigDecimal precioTotal) {
        this.vendedorId = vendedorId;
        this.jugadorId = jugadorId;
        this.cantidadTokens = cantidadTokens;
        this.precioPorToken = precioPorToken;
        this.precioTotal = precioTotal;
        this.estado = EstadoAnuncioVenta.PENDIENTE;
        this.creadoEn = LocalDateTime.now();
        this.actualizadoEn = this.creadoEn;
    }
}
