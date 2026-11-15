package com.prontaentrega.models.mercado;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import com.prontaentrega.models.jugadores.BolsaDeJugador;
import com.prontaentrega.models.auth.Usuario;
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
        setActualizadoEn(LocalDateTime.now(ZoneId.systemDefault()));
    }

    public void quitarTokens(Integer cantidadTokens) {
        this.tokensEnVenta = this.tokensEnVenta - cantidadTokens;
    }

    public void ponerTokensEnVenta(Integer cantidad) {

        if (getTokensDisponibles() < cantidad) {
            throw new CantidadDeTokensException("No posees suficientes tokens para la transacción");
        }

        setTokensDisponibles(getTokensDisponibles() - cantidad);
        setTokensEnVenta(getTokensEnVenta() + cantidad);
        setActualizadoEn(LocalDateTime.now(ZoneId.systemDefault()));
    }

    public void actualizarPorVenta(Integer cantidad, int cantidadTokensEnVenta) {
        if (cantidadTokensEnVenta > getTokensEnVenta()) {
            throw new CantidadDeTokensException("La publicación ya no tiene tokens disponibles");
        }

        this.quitarTokens(cantidad);
        this.setActualizadoEn(LocalDateTime.now(ZoneId.systemDefault()));
    }

    public void actualizarPorCompra(Integer cantidadSolicitada) {
        this.agregarTokens(cantidadSolicitada);
        this.setActualizadoEn(LocalDateTime.now(ZoneId.systemDefault()));
    }

    public void cancelarVenta(Integer cantidadTokens) {
        setTokensDisponibles(getTokensDisponibles() + cantidadTokens);
        setTokensEnVenta(getTokensEnVenta() - cantidadTokens);
        setActualizadoEn(LocalDateTime.now(ZoneId.systemDefault()));
    }
}
