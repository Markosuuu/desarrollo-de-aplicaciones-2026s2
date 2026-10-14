package com.prontaentrega.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa un jugador disponible en el catalogo local de ProntaEntrega.
 * La entidad concentra las invariantes de identidad y estadisticas para que el
 * servicio solo orqueste el proceso de actualizacion y persistencia.
 */
@Getter
@Setter
@NoArgsConstructor
public class Jugador {

    private UUID id;

    /**
     * Identificador del jugador en WhoScored.
     * Es la identidad externa estable del jugador.
     */
    private Integer whoscoredId;

    private String nombre;

    private String equipo;

    private String liga;

    private Integer edad;

    private Integer altura;

    private Integer peso;

    private String posicion;

    private Boolean activo;

    private Integer goles;

    private Integer asistencias;

    private BigDecimal disparosPorPartido;

    private BigDecimal porcentajePasesExitosos;

    private BigDecimal keyPassesPorPartido;

    private BigDecimal dribblesGanadosPorPartido;

    private BigDecimal faltasCometidasPorPartido;

    private BigDecimal rating;

    private Integer minutosJugados;

    /*
     * Datos propios de nuestra aplicación.
     */
    private BigDecimal cotizacionActual;

    private LocalDateTime fechaActualizacion;

    private String fuente;

    private Integer tokensRestantes;

    /**
     * Construye un jugador completo validando los campos obligatorios del catalogo.
     */
    public Jugador(
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
       validarIdentidad(whoscoredId, nombre, equipo, liga);

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

       this.cotizacionActual = Objects.requireNonNull(
               cotizacionActual, "La cotizacion es obligatoria");
       this.fechaActualizacion = Objects.requireNonNull(
               fechaActualizacion, "La fecha de actualizacion es obligatoria");
       this.fuente = validarTexto(fuente, "La fuente es obligatoria");
       this.tokensRestantes = Objects.requireNonNull(
               tokensRestantes, "Los tokens restantes son obligatorios");
    }

    /**
     * Crea un jugador proveniente de WhoScored con los valores comerciales por defecto.
     */
    public static Jugador desdeEstadisticas(
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
            LocalDateTime fecha) {

        validarEstadisticas(edad, altura, peso, posicion, activo, goles,
                asistencias, disparosPorPartido, keyPassesPorPartido,
                dribblesGanadosPorPartido, faltasCometidasPorPartido, rating,minutosJugados);

        return new Jugador(
                whoscoredId,
                nombre,
                equipo,
                liga,
                edad,
                altura,
                peso,
                posicion,
                activo,
                goles,
                asistencias,
                disparosPorPartido,
                keyPassesPorPartido,
                dribblesGanadosPorPartido,
                faltasCometidasPorPartido,
                rating,
                minutosJugados,
                BigDecimal.ONE,
                fecha,
                "WhoScored",
                100
        );
    }

    /**
     * Actualiza las estadisticas deportivas sin modificar los datos comerciales
     * del jugador.
     */
    public void actualizarEstadisticas(
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
            LocalDateTime fecha,
            String fuente) {

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

        this.fechaActualizacion = Objects.requireNonNull(
                fecha,
                "La fecha de actualizacion es obligatoria"
        );
        this.fuente = validarTexto(
                fuente,
                "La fuente es obligatoria"
        );
    }
    /**
     * Validaciones de identidad
     * */
    private static void validarIdentidad(Integer whoscoredId, String nombre, String equipo, String liga) {
        if (whoscoredId == null) {
            throw new IllegalArgumentException("El ID de WhoScored es obligatorio");
        }
        validarTexto(nombre, "El nombre es obligatorio");
        validarTexto(equipo, "El equipo es obligatorio");
        validarTexto(liga, "La liga es obligatoria");
    }

    private static String validarTexto(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    /**
     * Validaciones relacionadas a las estadisticas obtenidas.
     */
    private static void validarEstadisticas(
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
            Integer minutosJugados) {

        validarNoNegativo(edad, "La edad no puede ser negativa");
        validarNoNegativo(altura, "La altura no puede ser negativa");
        validarNoNegativo(peso, "El peso no puede ser negativo");

        validarNoNegativo(goles, "Los goles no pueden ser negativos");
        validarNoNegativo(asistencias, "Las asistencias no pueden ser negativas");

        validarNoNegativo(disparosPorPartido,
                "Los disparos por partido no pueden ser negativos");

        validarNoNegativo(keyPassesPorPartido,
                "Los key passes por partido no pueden ser negativos");

        validarNoNegativo(dribblesGanadosPorPartido,
                "Los dribbles ganados por partido no pueden ser negativos");

        validarNoNegativo(faltasCometidasPorPartido,
                "Las faltas cometidas por partido no pueden ser negativas");

        validarNoNegativo(rating,
                "El rating no puede ser negativo");

        validarNoNegativo(minutosJugados,
                "Los minutos jugados no pueden ser negativos");
    }

    private static void validarNoNegativo(Integer value, String message) {
        if (value != null && value < 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void validarNoNegativo(BigDecimal value, String message) {
        if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
