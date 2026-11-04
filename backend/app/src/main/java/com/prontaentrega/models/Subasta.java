package com.prontaentrega.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
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
            Usuario propietario,
            BolsaDeJugador bolsaDeJugador,
            Integer cantidadOfrecida,
            BigDecimal precioInicial,
            LocalDateTime vencimiento) {
        this.propietario = propietario;
        this.bolsaDeJugador = bolsaDeJugador;
        this.cantidadOfrecida = cantidadOfrecida;
        this.precioInicial = precioInicial;
        this.ofertaActual = null;
        this.activa = true;
        this.vencimiento = vencimiento;
        this.creadoEn = LocalDateTime.now(ZoneId.systemDefault());
        this.actualizadoEn = this.creadoEn;
    }

    public Subasta(
            Usuario propietario,
            BolsaDeJugador bolsaDeJugador,
            Integer cantidadOfrecida,
            BigDecimal precioInicial,
            LocalDateTime vencimiento,
            LocalDateTime creadoEn) {
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

    public UUID getJugadorId() {
        return bolsaDeJugador.getEstadisticasDeportivas().getJugador().getId();
    }

    public UUID getPropietarioId() {
        return propietario.getId();
    }

    public BigDecimal obtenerOfertaMinima() {
        return getOfertaActual() == null ? getPrecioInicial() : getOfertaActual().getPuja();
    }

    public void actualizarPostorMasGrande(Oferta oferta) {
        setOfertaActual(oferta);
        setActualizadoEn(LocalDateTime.now(ZoneId.systemDefault()));
    }

    public void cerrarPorVencimiento() {
        setActiva(false);
        setMotivoCierre("POR_TIEMPO");
        setActualizadoEn(LocalDateTime.now(ZoneId.systemDefault()));
    }
}
