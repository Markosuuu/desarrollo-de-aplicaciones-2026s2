package com.prontaentrega.models;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Clase que representa el inventario de un usuario en el sistema.
 * Relaciona un usuario con una cantidad de tokens de un jugador
 */
@Getter
@Setter
@NoArgsConstructor
public class InventarioUsuario {
    private UUID id;
    private Usuario usuario;
    private BolsaDeJugador bolsaDeJugador;
    private Integer tokensDisponibles;
    private Integer tokensEnVenta;
    private LocalDateTime actualizadoEn;

    public InventarioUsuario(
            Usuario usuario,
            BolsaDeJugador bolsaDeJugador,
            Integer tokensDisponibles,
            Integer tokensEnVenta) {
        this.usuario = usuario;
        this.bolsaDeJugador = bolsaDeJugador;
        this.tokensDisponibles = tokensDisponibles;
        this.tokensEnVenta = tokensEnVenta;
        this.actualizadoEn = LocalDateTime.now();
    }

    public void agregarTokens(Integer cantidadTokens) {
        this.tokensDisponibles = this.tokensDisponibles + cantidadTokens;
    }

    public void quitarTokens(Integer cantidadTokens) {
        this.tokensEnVenta = this.tokensEnVenta - cantidadTokens;
    }
}
