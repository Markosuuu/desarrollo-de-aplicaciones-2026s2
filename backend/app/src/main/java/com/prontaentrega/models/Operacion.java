package com.prontaentrega.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import com.prontaentrega.models.enums.EstadoOperacion;
import com.prontaentrega.models.enums.TipoOperacion;
import lombok.*;

/**
 * Nuevamente es un contenedor, igual este me parece necesario
 */

@Getter
@Setter
@Builder
@AllArgsConstructor
public class Operacion {
    private UUID id;
    private final Usuario usuario;
    private final Usuario contraparte;
    private final Jugador jugador;
    private final Integer cantidad;
    private final BigDecimal precioPorToken;
    private final BigDecimal total;
    private final TipoOperacion tipo;
    private final EstadoOperacion estado;
    private final LocalDateTime creadaEn;

    public Operacion(Usuario usuario, Usuario contraparte, Jugador jugador, Integer cantidad,
                    BigDecimal precioPorToken, BigDecimal total, TipoOperacion tipo,
                    EstadoOperacion estado) {
        this.usuario = usuario;
        this.contraparte = contraparte;
        this.jugador = jugador;
        this.cantidad = cantidad;
        this.precioPorToken = precioPorToken;
        this.total = total;
        this.tipo = tipo;
        this.estado = estado;
        this.creadaEn = LocalDateTime.now(ZoneId.systemDefault());
    }
}
