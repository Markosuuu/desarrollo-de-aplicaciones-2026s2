package com.prontaentrega.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa un jugador disponible en el catalogo local de ProntaEntrega.
 * La entidad concentra las invariantes de identidad y estadisticas para que el
 * servicio solo orqueste el proceso de actualizacion y persistencia.
 */
@Entity
@Table(name = "jugadores")
public class Jugador {

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

    /**
     * Constructor requerido por JPA.
     */
    public Jugador() {
    }
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

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEquipo() {
        return equipo;
    }

    public String getLiga() {
        return liga;
    }

    public BigDecimal getCotizacionActual() {
        return cotizacionActual;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public Integer getEdad() {
        return edad;
    }

    public Integer getGoles() {
        return goles;
    }

    public Integer getAsistencias() {
        return asistencias;
    }

    public BigDecimal getDisparosPorPartido() {
        return disparosPorPartido;
    }

    public BigDecimal getPorcentajePasesExitosos() {
        return porcentajePasesExitosos;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public BigDecimal getKeyPassesPorPartido() {
        return keyPassesPorPartido;
    }

    public void setKeyPassesPorPartido(BigDecimal keyPassesPorPartido) {
        this.keyPassesPorPartido = keyPassesPorPartido;
    }

    public BigDecimal getDribblesGanadosPorPartido() {
        return dribblesGanadosPorPartido;
    }

    public void setDribblesGanadosPorPartido(BigDecimal dribblesGanadosPorPartido) {
        this.dribblesGanadosPorPartido = dribblesGanadosPorPartido;
    }

    public BigDecimal getFaltasCometidasPorPartido() {
        return faltasCometidasPorPartido;
    }

    public void setFaltasCometidasPorPartido(BigDecimal faltasCometidasPorPartido) {
        this.faltasCometidasPorPartido = faltasCometidasPorPartido;
    }

    public Integer getAltura() {
        return altura;
    }

    public void setAltura(Integer altura) {
        this.altura = altura;
    }

    public Integer getPeso() {
        return peso;
    }

    public void setPeso(Integer peso) {
        this.peso = peso;
    }

    public String getPosicion() {
        return posicion;
    }

    public void setPosicion(String posicion) {
        this.posicion = posicion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Integer getMinutosJugados() {
        return minutosJugados;
    }

    public void setMinutosJugados(Integer minutosJugados) {
        this.minutosJugados = minutosJugados;
    }

    public Integer getTokensRestantes() {
        return tokensRestantes;
    }

    public void setTokensRestantes(Integer tokensRestantes) {
        this.tokensRestantes = tokensRestantes;
    }
}
