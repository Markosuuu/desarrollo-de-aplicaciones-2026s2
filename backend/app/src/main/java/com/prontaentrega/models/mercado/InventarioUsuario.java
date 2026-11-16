package com.prontaentrega.models.mercado;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import com.prontaentrega.models.exception.CampoFaltanteException;
import com.prontaentrega.models.exception.ValorNegativoException;
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
            @NonNull Usuario usuario,
            @NonNull BolsaDeJugador bolsaDeJugador,
            @NonNull Integer tokensDisponibles,
            @NonNull Integer tokensEnVenta) {

        if (tokensDisponibles < 0 || tokensEnVenta < 0) {
            throw new ValorNegativoException("Los tokens no pueden ser negativos");
        }

        this.usuario = usuario;
        this.bolsaDeJugador = bolsaDeJugador;
        this.tokensDisponibles = tokensDisponibles;
        this.tokensEnVenta = tokensEnVenta;
        this.actualizadoEn = LocalDateTime.now(ZoneId.systemDefault());
    }

    public void agregarTokens(Integer cantidadTokens) {
        validarCantidad(cantidadTokens);

        this.tokensDisponibles += cantidadTokens;
        this.actualizadoEn = LocalDateTime.now(ZoneId.systemDefault());
    }

    public void quitarTokens(Integer cantidadTokens) {
        validarCantidad(cantidadTokens);
        validarTokensEnVenta(cantidadTokens);

        this.tokensEnVenta -= cantidadTokens;
        this.actualizadoEn = LocalDateTime.now(ZoneId.systemDefault());
    }

    public void ponerTokensEnVenta(Integer cantidad) {
        validarCantidad(cantidad);

        if (tokensDisponibles < cantidad) {
            throw new CantidadDeTokensException("No posees suficientes tokens para la transacción");
        }

        this.tokensDisponibles -= cantidad;
        this.tokensEnVenta += cantidad;
        this.actualizadoEn = LocalDateTime.now(ZoneId.systemDefault());
    }

    public void actualizarPorVenta(Integer cantidad, int tokensEnVentaDelAnuncio) {
        validarCantidad(cantidad);

        if (tokensEnVentaDelAnuncio > this.tokensEnVenta) {
            throw new CantidadDeTokensException("La publicación ya no tiene tokens disponibles");
        }

        this.quitarTokens(cantidad);
    }

    public void actualizarPorCompra(Integer cantidadSolicitada) {
        this.agregarTokens(cantidadSolicitada);
    }

    public void cancelarVenta(Integer cantidadTokens) {
        validarCantidad(cantidadTokens);
        validarTokensEnVenta(cantidadTokens);

        this.tokensDisponibles += cantidadTokens;
        this.tokensEnVenta -= cantidadTokens;
        this.actualizadoEn = LocalDateTime.now(ZoneId.systemDefault());
    }

    private void validarCantidad(Integer cantidad) {
        if (cantidad == null) {
            throw new CampoFaltanteException("Cantidad inválida");
        }

        if (cantidad <= 0) {
            throw new ValorNegativoException("La cantidad debe ser mayor que 0");
        }
    }

    private void validarTokensEnVenta(Integer cantidad) {
        if (cantidad > this.tokensEnVenta) {
            throw new CantidadDeTokensException("No hay suficientes tokens en venta");
        }
    }
}
