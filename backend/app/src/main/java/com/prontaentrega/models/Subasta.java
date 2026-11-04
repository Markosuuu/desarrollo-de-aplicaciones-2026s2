package com.prontaentrega.models;

import com.prontaentrega.models.enums.EstadoSubasta;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Subasta {
    private UUID id;
    private Usuario propietario;
    private BolsaDeJugador bolsaDeJugador;
    private Integer cantidadOfrecida;
    private BigDecimal precioInicial;
    private Oferta ofertaActual;
    private EstadoSubasta estado;
    private LocalDateTime vencimiento;
    private LocalDateTime creadoEn;
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
        this.estado = EstadoSubasta.ACTIVA;
        this.vencimiento = vencimiento;
        this.creadoEn = LocalDateTime.now(ZoneId.systemDefault());
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
}
