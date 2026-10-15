package com.prontaentrega.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Clase que representa la bolsa de un jugador
 * Esto sería datos de cotización, tokens restantes, información comercial vease.
 * <br><br>
 * Contiene las estadísticas deportivas de un jugador así poder recalcularse y/o acceder a un jugador también.
 * <br><br>
 * La nombré BolsaDeJugador porque era un concepto relacionado con el mercado,
 * si existen mejores nombres, se aceptan sugerencias :)
 *
 * @author ProntaEntrega
 * @version 1.0
 * @since 2024-06-15
 */
@Getter
@Setter
@NoArgsConstructor
public class BolsaDeJugador {
    private UUID id;
    private EstadisticasDeportivas estadisticasDeportivas;
    private BigDecimal cotizacionActual;
    private LocalDateTime fechaActualizacion;
    private String fuente;
    private Integer tokensRestantes;

    /**
     * Constructor de la clase BolsaDeJugador
     * @param estadisticasDeportivas Las estadísticas deportivas del jugador
     * @param cotizacionActual La cotización actual del jugador
     * @param fechaActualizacion La fecha de actualización de la bolsa
     * @param fuente La fuente de los datos
     * @param tokensRestantes Los tokens restantes en la bolsa
     */
    public BolsaDeJugador(
            @NonNull EstadisticasDeportivas estadisticasDeportivas,
            @NonNull BigDecimal cotizacionActual,
            @NonNull LocalDateTime fechaActualizacion,
            @NonNull String fuente,
            @NonNull Integer tokensRestantes) {
        this.estadisticasDeportivas = estadisticasDeportivas;
        this.cotizacionActual = cotizacionActual;
        this.fechaActualizacion = fechaActualizacion;
        this.fuente = fuente;
        this.tokensRestantes = tokensRestantes;
    }

    public static BolsaDeJugador desdeEstadisticas(
            Integer whoscoredId,
            String nombre,
            String equipo,
            Liga liga,
            Integer edad,
            Integer altura,
            Integer peso,
            Posicion posicion,
            Boolean activo,
            Integer goles,
            Integer asistencias,
            BigDecimal disparosPorPartido,
            BigDecimal keyPassesPorPartido,
            BigDecimal dribblesGanadosPorPartido,
            BigDecimal faltasCometidasPorPartido,
            BigDecimal rating,
            Integer minutosJugados,
            LocalDateTime fecha) {
        Jugador jugador = new Jugador(
                whoscoredId, nombre, equipo, liga, posicion, edad, altura, peso);
        EstadisticasDeportivas estadisticas = new EstadisticasDeportivas(
                jugador, activo, goles, asistencias, disparosPorPartido, null,
                keyPassesPorPartido, dribblesGanadosPorPartido,
                faltasCometidasPorPartido, rating, minutosJugados);
        return new BolsaDeJugador(
                estadisticas, BigDecimal.ONE, fecha, "WhoScored", 100);
    }

    public void actualizarEstadisticas(
            Integer edad,
            Integer altura,
            Integer peso,
            Posicion posicion,
            Boolean activo,
            Integer goles,
            Integer asistencias,
            BigDecimal disparosPorPartido,
            BigDecimal keyPassesPorPartido,
            BigDecimal dribblesGanadosPorPartido,
            BigDecimal faltasCometidasPorPartido,
            BigDecimal rating,
            Integer minutosJugados,
            @NonNull LocalDateTime fecha,
            @NonNull String fuente) {
        estadisticasDeportivas.getJugador().actualizar(edad, altura, peso, posicion);
        estadisticasDeportivas.actualizar(
                activo, goles, asistencias,
                disparosPorPartido, keyPassesPorPartido, dribblesGanadosPorPartido,
                faltasCometidasPorPartido, rating, minutosJugados);
        this.fechaActualizacion = fecha;
        this.fuente = fuente;
    }
}
