package com.prontaentrega.models;

import java.math.BigDecimal;
import java.util.UUID;

import com.prontaentrega.models.builders.EstadisticasDeportivasDatos;
import com.prontaentrega.models.exception.ValorNegativoException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Clase que representa las estadísticas deportivas de un jugador
 * Contiene información sobre el rendimiento del jugador en partidos
 */
@Getter
@Setter
@NoArgsConstructor
public class EstadisticasDeportivas {
    private UUID id;
    private Jugador jugador;
    private Boolean activo;
    private Integer goles;
    private Integer asistencias;
    private BigDecimal disparosPorPartido;
    private BigDecimal keyPassesPorPartido;
    private BigDecimal dribblesGanadosPorPartido;
    private BigDecimal faltasCometidasPorPartido;
    private BigDecimal rating;
    private Integer minutosJugados;

    /**
     * Constructor de estadísticas
     * @param jugador El jugador a la que están asociadas las estadísticas
     * @param activo Si sigue jugando actualmente
     * @param goles Cantidad de goles
     * @param asistencias Cantidad de asistencias
     * @param disparosPorPartido Número de disparos por partido
     * @param keyPassesPorPartido Número de key passes por partido
     * @param dribblesGanadosPorPartido Número de dribbles ganados por partido
     * @param faltasCometidasPorPartido Número de faltas cometidas por partido
     * @param rating Rating del jugador
     * @param minutosJugados Minutos jugados
     */
    public EstadisticasDeportivas(
            @NonNull Jugador jugador,
            Boolean activo,
            Integer goles,
            Integer asistencias,
            BigDecimal disparosPorPartido,
            BigDecimal keyPassesPorPartido,
            BigDecimal dribblesGanadosPorPartido,
            BigDecimal faltasCometidasPorPartido,
            BigDecimal rating,
            Integer minutosJugados) {

        // Dejo esto acá porque Sonar llora por el metodo que quiero generar
        // Ya que son 8 parámetros y el límite son 7 :/
        validarNoNegativo(goles, "Los goles no pueden ser negativos");
        validarNoNegativo(asistencias, "Las asistencias no pueden ser negativas");
        validarNoNegativo(disparosPorPartido, "Los disparos por partido no pueden ser negativos");
        validarNoNegativo(keyPassesPorPartido, "Los key passes por partido no pueden ser negativos");
        validarNoNegativo(dribblesGanadosPorPartido, "Los dribbles ganados por partido no pueden ser negativos");
        validarNoNegativo(faltasCometidasPorPartido, "Las faltas cometidas por partido no pueden ser negativas");
        validarNoNegativo(rating, "El rating no puede ser negativo");
        validarNoNegativo(minutosJugados, "Los minutos jugados no pueden ser negativos");

        this.jugador = jugador;
        this.activo = activo;
        this.goles = goles;
        this.asistencias = asistencias;
        this.disparosPorPartido = disparosPorPartido;
        this.keyPassesPorPartido = keyPassesPorPartido;
        this.dribblesGanadosPorPartido = dribblesGanadosPorPartido;
        this.faltasCometidasPorPartido = faltasCometidasPorPartido;
        this.rating = rating;
        this.minutosJugados = minutosJugados;
    }

    public void actualizar(EstadisticasDeportivasDatos datos) {
        validar(datos);
        this.jugador = datos.getJugador();
        this.activo = datos.getActivo();
        this.goles = datos.getGoles();
        this.asistencias = datos.getAsistencias();
        this.disparosPorPartido = datos.getDisparosPorPartido();
        this.keyPassesPorPartido = datos.getKeyPassesPorPartido();
        this.dribblesGanadosPorPartido = datos.getDribblesGanadosPorPartido();
        this.faltasCometidasPorPartido = datos.getFaltasCometidasPorPartido();
        this.rating = datos.getRating();
        this.minutosJugados = datos.getMinutosJugados();
    }

    private static void validar(EstadisticasDeportivasDatos datos) {
        validarNoNegativo(datos.getGoles(), "Los goles no pueden ser negativos");
        validarNoNegativo(datos.getAsistencias(), "Las asistencias no pueden ser negativas");
        validarNoNegativo(datos.getDisparosPorPartido(), "Los disparos por partido no pueden ser negativos");
        validarNoNegativo(datos.getKeyPassesPorPartido(), "Los key passes por partido no pueden ser negativos");
        validarNoNegativo(datos.getDribblesGanadosPorPartido(), "Los dribbles ganados por partido no pueden ser negativos");
        validarNoNegativo(datos.getFaltasCometidasPorPartido(), "Las faltas cometidas por partido no pueden ser negativas");
        validarNoNegativo(datos.getRating(), "El rating no puede ser negativo");
        validarNoNegativo(datos.getMinutosJugados(), "Los minutos jugados no pueden ser negativos");
    }

    private static void validarNoNegativo(Integer value, String message) {
        if (value != null && value < 0) {
            throw new ValorNegativoException(message);
        }
    }

    private static void validarNoNegativo(BigDecimal value, String message) {
        if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValorNegativoException(message);
        }
    }
}
