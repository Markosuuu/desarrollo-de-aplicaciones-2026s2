package com.prontaentrega.models.subastas;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import com.prontaentrega.models.exception.OfertaInvalidaException;
import com.prontaentrega.models.exception.ValorNegativoException;
import com.prontaentrega.models.exception.VencimientoInvalidoException;
import com.prontaentrega.models.jugadores.BolsaDeJugador;
import com.prontaentrega.models.auth.Usuario;
import com.prontaentrega.models.exception.SubastaNoActivaException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Subasta {
    private UUID id;
    private final Usuario propietario;
    private final BolsaDeJugador bolsaDeJugador;
    private final Integer cantidadOfrecida;
    private final BigDecimal precioInicial;
    private final LocalDateTime vencimiento;
    private final LocalDateTime creadoEn;
    private Oferta ofertaActual;
    private Boolean activa;
    private LocalDateTime actualizadoEn;
    private String motivoCierre;

    public Subasta(
            @NonNull Usuario propietario,
            @NonNull BolsaDeJugador bolsaDeJugador,
            @NonNull Integer cantidadOfrecida,
            @NonNull BigDecimal precioInicial,
            @NonNull LocalDateTime vencimiento) {
        this(propietario, bolsaDeJugador, cantidadOfrecida, precioInicial, vencimiento,
                LocalDateTime.now(ZoneId.systemDefault()));
    }

    public Subasta(
            @NonNull Usuario propietario,
            @NonNull BolsaDeJugador bolsaDeJugador,
            @NonNull Integer cantidadOfrecida,
            @NonNull BigDecimal precioInicial,
            @NonNull LocalDateTime vencimiento,
            @NonNull LocalDateTime creadoEn) {

        if (cantidadOfrecida <= 0) {
            throw new ValorNegativoException("La cantidad ofrecida debe ser mayor que 0");
        }

        if (precioInicial.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValorNegativoException("El precio inicial debe ser mayor que 0");
        }

        if (!vencimiento.isAfter(creadoEn)) {
            throw new VencimientoInvalidoException("El vencimiento debe ser posterior a la creación");
        }

        this.propietario = propietario;
        this.bolsaDeJugador = bolsaDeJugador;
        this.cantidadOfrecida = cantidadOfrecida;
        this.precioInicial = precioInicial;
        this.ofertaActual = null;
        this.activa = true;
        this.vencimiento = vencimiento;
        this.creadoEn = creadoEn;
        this.actualizadoEn = this.creadoEn;
    }

    public void actualizarPostorMasGrande(@NonNull Oferta oferta) {
        if (!activa) {
            throw new SubastaNoActivaException("La subasta no está activa");
        }

        if (!LocalDateTime.now(ZoneId.systemDefault()).isBefore(vencimiento)) {
            throw new SubastaNoActivaException("La subasta ya venció");
        }

        if (!esPujaSuficiente(oferta.getPuja())) {
            throw new OfertaInvalidaException("La oferta debe superar la oferta mínima");
        }

        this.ofertaActual = oferta;
        this.actualizadoEn = LocalDateTime.now(ZoneId.systemDefault());
    }

    public void cerrarSubasta(@NonNull String motivo) {
        if (!activa) {
            throw new SubastaNoActivaException("La subasta ya está cerrada");
        }

        this.motivoCierre = motivo;
        this.activa = false;
        this.actualizadoEn = LocalDateTime.now(ZoneId.systemDefault());
    }

    public BigDecimal obtenerOfertaMinima() {
        return getOfertaActual() == null
                ? getPrecioInicial()
                : getOfertaActual().getPuja();
    }

    private boolean esPujaSuficiente(BigDecimal puja) {
        int comparacion = puja.compareTo(obtenerOfertaMinima());
        return ofertaActual == null ? comparacion >= 0 : comparacion > 0;
    }
}
