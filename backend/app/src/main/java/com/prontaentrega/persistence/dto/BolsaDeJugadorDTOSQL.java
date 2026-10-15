package com.prontaentrega.persistence.dto;

import com.prontaentrega.models.BolsaDeJugador;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "BolsaDeJugador")
@Table(name = "bolsas_de_jugadores")
@Getter
@Setter
@NoArgsConstructor
public class BolsaDeJugadorDTOSQL {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "estadisticas_deportivas_id", nullable = false, unique = true)
    private EstadisticasDeportivasDTOSQL estadisticasDeportivas;

    @Column(name = "cotizacion_actual", nullable = false)
    private BigDecimal cotizacionActual;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @Column(nullable = false)
    private String fuente;

    @Column(name = "tokens_restantes", nullable = false)
    private Integer tokensRestantes;

    public static BolsaDeJugadorDTOSQL desdeModelo(BolsaDeJugador bolsa) {
        BolsaDeJugadorDTOSQL dto = new BolsaDeJugadorDTOSQL();
        dto.id = bolsa.getId();
        dto.estadisticasDeportivas =
                EstadisticasDeportivasDTOSQL.desdeModelo(bolsa.getEstadisticasDeportivas());
        dto.cotizacionActual = bolsa.getCotizacionActual();
        dto.fechaActualizacion = bolsa.getFechaActualizacion();
        dto.fuente = bolsa.getFuente();
        dto.tokensRestantes = bolsa.getTokensRestantes();
        return dto;
    }

    public BolsaDeJugador aModelo() {
        BolsaDeJugador bolsa = new BolsaDeJugador(
                estadisticasDeportivas.aModelo(),
                cotizacionActual,
                fechaActualizacion,
                fuente,
                tokensRestantes);
        bolsa.setId(id);
        return bolsa;
    }
}
