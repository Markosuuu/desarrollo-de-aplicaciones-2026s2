package com.prontaentrega.models.mercado;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import com.prontaentrega.models.exception.CampoFaltanteException;
import com.prontaentrega.models.exception.CantidadDeTokensException;
import com.prontaentrega.models.exception.ValorNegativoException;
import com.prontaentrega.models.exception.AnuncioNoPendienteException;
import com.prontaentrega.models.jugadores.BolsaDeJugador;
import com.prontaentrega.models.auth.Usuario;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AnuncioVenta {
    private UUID id;
    private final Usuario vendedor;
    private final BolsaDeJugador bolsaDeJugador;
    private final BigDecimal precioPorToken;
    private final LocalDateTime creadoEn;
    private Integer cantidadTokens;
    private BigDecimal precioTotal;
    private EstadoAnuncioVenta estado;
    private LocalDateTime actualizadoEn;

    /**
     * Constructor de anuncios para vender
     * @param vendedor El vendedor del anuncio
     * @param bolsaDeJugador La bolsa de jugador que se está vendiendo
     * @param cantidadTokens La cantidad de tokens que se están vendiendo
     */
    public AnuncioVenta(
            @NonNull Usuario vendedor,
            @NonNull BolsaDeJugador bolsaDeJugador,
            @NonNull Integer cantidadTokens) {

        if (cantidadTokens <= 0) {
            throw new ValorNegativoException("La cantidad a vender no puede ser 0 o negativa");
        }

        this.vendedor = vendedor;
        this.bolsaDeJugador = bolsaDeJugador;
        this.cantidadTokens = cantidadTokens;
        this.precioPorToken = bolsaDeJugador.getCotizacionActual();
        this.precioTotal = this.precioPorToken.multiply(BigDecimal.valueOf(cantidadTokens));
        this.estado = EstadoAnuncioVenta.PENDIENTE;
        this.creadoEn = LocalDateTime.now(ZoneId.systemDefault());
        this.actualizadoEn = this.creadoEn;
    }

    public void actualizarTokensEnVenta(Integer cantidadSolicitada) {

        validarActualizacion(cantidadSolicitada);

        this.cantidadTokens -= cantidadSolicitada;
        this.precioTotal = this.precioPorToken.multiply(BigDecimal.valueOf(this.cantidadTokens));

        if (cantidadTokens == 0) {
            this.estado = EstadoAnuncioVenta.COMPLETADA;
        }

        this.actualizadoEn = LocalDateTime.now(ZoneId.systemDefault());
    }

    private void validarActualizacion(Integer cantidadSolicitada) {
        if (cantidadSolicitada == null) {
            throw new CampoFaltanteException("Cantidad inválida");
        }

        if (cantidadSolicitada <= 0) {
            throw new ValorNegativoException("La cantidad no puede ser negativa");
        }

        if (this.cantidadTokens < cantidadSolicitada) {
            throw new CantidadDeTokensException("No hay suficientes tokens");
        }
    }

    public void cancelarAnuncio() {
        if (!estado.equals(EstadoAnuncioVenta.PENDIENTE))
            throw new AnuncioNoPendienteException("El anuncio no está pendiente para cancelarlo");

        this.estado = EstadoAnuncioVenta.CANCELADA;
        this.actualizadoEn = LocalDateTime.now(ZoneId.systemDefault());
    }
}
