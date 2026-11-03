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
    private Usuario usuario;
    private Usuario contraparte;
    private Jugador jugador;
    private Integer cantidad;
    private BigDecimal precioPorToken;
    private BigDecimal total;
    private TipoOperacion tipo;
    private EstadoOperacion estado;
    private LocalDateTime creadaEn;

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
        this.creadaEn = LocalDateTime.now();
    }
}
