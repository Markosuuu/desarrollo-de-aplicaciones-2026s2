package com.prontaentrega.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.prontaentrega.models.enums.EstadoOperacion;
import com.prontaentrega.models.enums.TipoOperacion;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Nuevamente es un contenedor, igual este me parece necesario
 */

@Getter
@Setter
@NoArgsConstructor
public class Operacion {
    private UUID id;
    private UUID usuarioId;
    private UUID contraparteId;
    private String usuarioNombre;
    private String contraparteNombre;
    private UUID jugadorId;
    private Integer cantidad;
    private BigDecimal precioPorToken;
    private BigDecimal total;
    private TipoOperacion tipo;
    private EstadoOperacion estado;
    private String nombreJugador;
    private LocalDateTime creadaEn;

    public Operacion(UUID usuarioId, UUID contraparteId, String usuarioNombre, String contraparteNombre, UUID jugadorId, Integer cantidad,
                    BigDecimal precioPorToken, BigDecimal total, TipoOperacion tipo,
                    EstadoOperacion estado, String nombreJugador) {
        this.usuarioId = usuarioId;
        this.contraparteId = contraparteId;
        this.usuarioNombre = usuarioNombre;
        this.contraparteNombre = contraparteNombre;
        this.jugadorId = jugadorId;
        this.cantidad = cantidad;
        this.precioPorToken = precioPorToken;
        this.total = total;
        this.tipo = tipo;
        this.estado = estado;
        this.nombreJugador = nombreJugador;
        this.creadaEn = LocalDateTime.now();
    }
}
