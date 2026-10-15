package com.prontaentrega.persistence.dto;

import com.prontaentrega.models.EstadisticasDeportivas;
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
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "EstadisticasDeportivas")
@Table(name = "estadisticas_deportivas")
@Getter
@Setter
@NoArgsConstructor
public class EstadisticasDeportivasDTOSQL {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "jugador_id", nullable = false, unique = true)
    private JugadorDTOSQL jugador;

    @Column(nullable = false)
    private Boolean activo;

    @Column(nullable = false)
    private Integer goles;

    @Column(nullable = false)
    private Integer asistencias;

    @Column(name = "disparos_por_partido")
    private BigDecimal disparosPorPartido;

    @Column(name = "porcentaje_pases_exitosos")
    private BigDecimal porcentajePasesExitosos;

    @Column(name = "key_passes_por_partido")
    private BigDecimal keyPassesPorPartido;

    @Column(name = "dribbles_ganados_por_partido")
    private BigDecimal dribblesGanadosPorPartido;

    @Column(name = "faltas_cometidas_por_partido")
    private BigDecimal faltasCometidasPorPartido;

    @Column
    private BigDecimal rating;

    @Column(name = "minutos_jugados")
    private Integer minutosJugados;

    static EstadisticasDeportivasDTOSQL desdeModelo(EstadisticasDeportivas estadisticas) {
        EstadisticasDeportivasDTOSQL dto = new EstadisticasDeportivasDTOSQL();
        dto.id = estadisticas.getId();
        dto.jugador = JugadorDTOSQL.desdeJugador(estadisticas.getJugador());
        dto.activo = estadisticas.getActivo();
        dto.goles = estadisticas.getGoles();
        dto.asistencias = estadisticas.getAsistencias();
        dto.disparosPorPartido = estadisticas.getDisparosPorPartido();
        dto.porcentajePasesExitosos = estadisticas.getPorcentajePasesExitosos();
        dto.keyPassesPorPartido = estadisticas.getKeyPassesPorPartido();
        dto.dribblesGanadosPorPartido = estadisticas.getDribblesGanadosPorPartido();
        dto.faltasCometidasPorPartido = estadisticas.getFaltasCometidasPorPartido();
        dto.rating = estadisticas.getRating();
        dto.minutosJugados = estadisticas.getMinutosJugados();
        return dto;
    }

    EstadisticasDeportivas aModelo() {
        EstadisticasDeportivas estadisticas = new EstadisticasDeportivas(
                jugador.aJugador(), activo, goles, asistencias, disparosPorPartido,
                porcentajePasesExitosos, keyPassesPorPartido, dribblesGanadosPorPartido,
                faltasCometidasPorPartido, rating, minutosJugados);
        estadisticas.setId(id);
        return estadisticas;
    }
}
