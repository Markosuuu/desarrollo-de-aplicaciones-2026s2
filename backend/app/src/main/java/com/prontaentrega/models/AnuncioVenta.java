package com.prontaentrega.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * La clase está bien, quizás haría falta darle una vuelta al tema de los anuncios
 * porque es una clase que contiene datos nomás
 */

@Getter
@Setter
@NoArgsConstructor
public class AnuncioVenta {
    private UUID id;
    private Usuario vendedor;
    private BolsaDeJugador bolsaDeJugador;
    private Integer cantidadTokens;
    private BigDecimal precioPorToken;
    private BigDecimal precioTotal;
    private EstadoAnuncioVenta estado;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    /**
     * Constructor de anuncios para vender
     * @param vendedor El vendedor del anuncio
     * @param bolsaDeJugador La bolsa de jugador que se está vendiendo
     * @param cantidadTokens La cantidad de tokens que se están vendiendo
     */
    public AnuncioVenta(Usuario vendedor, BolsaDeJugador bolsaDeJugador, Integer cantidadTokens) {
        this.vendedor = vendedor;
        this.bolsaDeJugador = bolsaDeJugador;
        this.cantidadTokens = cantidadTokens;
        this.precioPorToken = bolsaDeJugador.getCotizacionActual();
        this.precioTotal = this.precioPorToken.multiply(BigDecimal.valueOf(cantidadTokens));
        this.estado = EstadoAnuncioVenta.PENDIENTE;
        this.creadoEn = LocalDateTime.now();
        this.actualizadoEn = this.creadoEn;
    }
}
