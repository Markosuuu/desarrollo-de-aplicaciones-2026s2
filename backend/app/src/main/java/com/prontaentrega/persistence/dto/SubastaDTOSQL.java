package com.prontaentrega.persistence.dto;

import com.prontaentrega.models.Subasta;
import com.prontaentrega.models.enums.EstadoSubasta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "Subasta")
@Table(name = "subastas")
@Getter
@Setter
@NoArgsConstructor
public class SubastaDTOSQL {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "propietario_id", nullable = false)
    private UsuarioDTOSQL propietario;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "bolsa_de_jugador_id", nullable = false)
    private BolsaDeJugadorDTOSQL bolsaDeJugador;

    @Column(name = "cantidad_ofrecida", nullable = false)
    private Integer cantidadOfrecida;

    @Column(name = "precio_inicial", nullable = false)
    private BigDecimal precioInicial;

    @Column(name = "oferta_actual")
    private BigDecimal ofertaActual;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ofertante_actual_id")
    private UsuarioDTOSQL ofertanteActual;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSubasta estado;

    @Column(nullable = false)
    private LocalDateTime vencimiento;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    @Column(name = "motivo_cierre")
    private String motivoCierre;

    @Version
    private Long version;

    public static SubastaDTOSQL desdeModelo(
            Subasta subasta,
            UsuarioDTOSQL propietario,
            BolsaDeJugadorDTOSQL bolsaDeJugador,
            UsuarioDTOSQL ofertanteActual) {
        SubastaDTOSQL dto = new SubastaDTOSQL();
        dto.id = subasta.getId();
        dto.propietario = propietario;
        dto.bolsaDeJugador = bolsaDeJugador;
        dto.cantidadOfrecida = subasta.getCantidadOfrecida();
        dto.precioInicial = subasta.getPrecioInicial();
        dto.ofertaActual = subasta.getOfertaActual();
        dto.ofertanteActual = ofertanteActual;
        dto.estado = subasta.getEstado();
        dto.vencimiento = subasta.getVencimiento();
        dto.creadoEn = subasta.getCreadoEn();
        dto.actualizadoEn = subasta.getActualizadoEn();
        dto.motivoCierre = subasta.getMotivoCierre();
        return dto;
    }

    public Subasta aModelo() {
        Subasta subasta = new Subasta(
                UsuarioDTOSQL.aUsuario(propietario),
                bolsaDeJugador.aModelo(),
                cantidadOfrecida,
                precioInicial,
                vencimiento);
        subasta.setId(id);
        subasta.setOfertaActual(ofertaActual);
        subasta.setOfertanteActual(ofertanteActual == null ? null : UsuarioDTOSQL.aUsuario(ofertanteActual));
        subasta.setEstado(estado);
        subasta.setCreadoEn(creadoEn);
        subasta.setActualizadoEn(actualizadoEn);
        subasta.setMotivoCierre(motivoCierre);
        return subasta;
    }
}
