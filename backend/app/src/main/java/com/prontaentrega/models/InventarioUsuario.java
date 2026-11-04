package com.prontaentrega.models;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import com.prontaentrega.models.exception.CantidadDeTokensException;
import lombok.*;

/**
 * Clase que representa el inventario de un usuario en el sistema.
 * Relaciona un usuario con una cantidad de tokens de un jugador
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class InventarioUsuario {
    private UUID id;
    private final Usuario usuario;
    private final BolsaDeJugador bolsaDeJugador;
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
        this.actualizadoEn = LocalDateTime.now(ZoneId.systemDefault());
    }

    public void agregarTokens(Integer cantidadTokens) {
        this.tokensDisponibles = this.tokensDisponibles + cantidadTokens;
    }

    public void quitarTokens(Integer cantidadTokens) {
        this.tokensEnVenta = this.tokensEnVenta - cantidadTokens;
    }

    public void ponerTokensEnVenta(Integer cantidad) {

        if (getTokensDisponibles() < cantidad) {
            throw new CantidadDeTokensException("No posees suficientes tokens para subastar");
        }

        setTokensDisponibles(getTokensDisponibles() - cantidad);
        setTokensEnVenta(getTokensEnVenta() + cantidad);
        setActualizadoEn(LocalDateTime.now(ZoneId.systemDefault()));
    }
}
