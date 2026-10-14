package com.prontaentrega.persistence.dto;

import com.prontaentrega.models.Jugador;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "Jugador")
@Table(name = "jugadores")
@Getter
@Setter
@NoArgsConstructor
public class JugadorDTOSQL {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Identificador del jugador en WhoScored.
     * Es la identidad externa estable del jugador.
     */
    @Column(name = "whoscored_id", nullable = false, unique = true)
    private Integer whoscoredId;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String equipo;

    @Column(nullable = false)
    private String liga;

    @Column(nullable = false)
    private Integer edad;

    @Column
    private Integer altura;

    @Column
    private Integer peso;

    @Column
    private String posicion;

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

    /*
     * Datos propios de nuestra aplicación.
     */
    @Column(name = "cotizacion_actual", nullable = false)
    private BigDecimal cotizacionActual;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @Column(nullable = false)
    private String fuente;

    @Column(name = "tokens_restantes", nullable = false)
    private Integer tokensRestantes;

    public JugadorDTOSQL(
            Integer whoscoredId,
            String nombre,
            String equipo,
            String liga,
            Integer edad,
            Integer altura,
            Integer peso,
            String posicion,
            Boolean activo,
            Integer goles,
            Integer asistencias,
            BigDecimal disparosPorPartido,
            BigDecimal keyPassesPorPartido,
            BigDecimal dribblesGanadosPorPartido,
            BigDecimal faltasCometidasPorPartido,
            BigDecimal rating,
            Integer minutosJugados,
            BigDecimal cotizacionActual,
            LocalDateTime fechaActualizacion,
            String fuente,
            Integer tokensRestantes) {

        this.whoscoredId = whoscoredId;
        this.nombre = nombre.trim();
        this.equipo = equipo.trim();
        this.liga = liga.trim();

        this.edad = edad;
        this.altura = altura;
        this.peso = peso;
        this.posicion = posicion;
        this.activo = activo;

        this.goles = goles;
        this.asistencias = asistencias;
        this.disparosPorPartido = disparosPorPartido;
        this.keyPassesPorPartido = keyPassesPorPartido;
        this.dribblesGanadosPorPartido = dribblesGanadosPorPartido;
        this.faltasCometidasPorPartido = faltasCometidasPorPartido;
        this.rating = rating;
        this.minutosJugados = minutosJugados;

        this.cotizacionActual = cotizacionActual;
        this.fechaActualizacion = fechaActualizacion;
        this.fuente = fuente;
        this.tokensRestantes = tokensRestantes;
    }

    public static JugadorDTOSQL desdeJugador(Jugador jugador) {
        JugadorDTOSQL dto = new JugadorDTOSQL();
        dto.setId(jugador.getId());
        dto.setWhoscoredId(jugador.getWhoscoredId());
        dto.setNombre(jugador.getNombre());
        dto.setEquipo(jugador.getEquipo());
        dto.setLiga(jugador.getLiga());
        dto.setEdad(jugador.getEdad());
        dto.setAltura(jugador.getAltura());
        dto.setPeso(jugador.getPeso());
        dto.setPosicion(jugador.getPosicion());
        dto.setActivo(jugador.getActivo());
        dto.setGoles(jugador.getGoles());
        dto.setAsistencias(jugador.getAsistencias());
        dto.setDisparosPorPartido(jugador.getDisparosPorPartido());
        dto.setPorcentajePasesExitosos(jugador.getPorcentajePasesExitosos());
        dto.setKeyPassesPorPartido(jugador.getKeyPassesPorPartido());
        dto.setDribblesGanadosPorPartido(jugador.getDribblesGanadosPorPartido());
        dto.setFaltasCometidasPorPartido(jugador.getFaltasCometidasPorPartido());
        dto.setRating(jugador.getRating());
        dto.setMinutosJugados(jugador.getMinutosJugados());
        dto.setCotizacionActual(jugador.getCotizacionActual());
        dto.setFechaActualizacion(jugador.getFechaActualizacion());
        dto.setFuente(jugador.getFuente());
        dto.setTokensRestantes(jugador.getTokensRestantes());

        return dto;
    }

    public static Jugador aJugador(JugadorDTOSQL jugadorDTOSQL) {
        Jugador jugador = new Jugador();
        jugador.setId(jugadorDTOSQL.getId());
        jugador.setWhoscoredId(jugadorDTOSQL.getWhoscoredId());
        jugador.setNombre(jugadorDTOSQL.getNombre());
        jugador.setEquipo(jugadorDTOSQL.getEquipo());
        jugador.setLiga(jugadorDTOSQL.getLiga());
        jugador.setEdad(jugadorDTOSQL.getEdad());
        jugador.setAltura(jugadorDTOSQL.getAltura());
        jugador.setPeso(jugadorDTOSQL.getPeso());
        jugador.setPosicion(jugadorDTOSQL.getPosicion());
        jugador.setActivo(jugadorDTOSQL.getActivo());
        jugador.setGoles(jugadorDTOSQL.getGoles());
        jugador.setAsistencias(jugadorDTOSQL.getAsistencias());
        jugador.setDisparosPorPartido(jugadorDTOSQL.getDisparosPorPartido());
        jugador.setPorcentajePasesExitosos(jugadorDTOSQL.getPorcentajePasesExitosos());
        jugador.setKeyPassesPorPartido(jugadorDTOSQL.getKeyPassesPorPartido());
        jugador.setDribblesGanadosPorPartido(jugadorDTOSQL.getDribblesGanadosPorPartido());
        jugador.setFaltasCometidasPorPartido(jugadorDTOSQL.getFaltasCometidasPorPartido());
        jugador.setRating(jugadorDTOSQL.getRating());
        jugador.setMinutosJugados(jugadorDTOSQL.getMinutosJugados());
        jugador.setCotizacionActual(jugadorDTOSQL.getCotizacionActual());
        jugador.setFechaActualizacion(jugadorDTOSQL.getFechaActualizacion());
        jugador.setFuente(jugadorDTOSQL.getFuente());
        jugador.setTokensRestantes(jugadorDTOSQL.getTokensRestantes());

        return jugador;
    }

}
