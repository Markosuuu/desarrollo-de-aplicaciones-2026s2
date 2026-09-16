package com.prontaentrega.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "jugadores")
public class Jugador {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String equipo;

    @Column(nullable = false)
    private String liga;

    @Column(name = "cotizacion_actual", nullable = false)
    private BigDecimal cotizacionActual;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @Column(nullable = false)
    private String fuente;

    @Column(name = "tokens_restantes", nullable = false)
    private Integer tokensRestantes;

    public Jugador() {
    }

    public Jugador(String nombre, String equipo, String liga, BigDecimal cotizacionActual,
                   LocalDateTime fechaActualizacion, String fuente, Integer tokensRestantes) {
        this.nombre = nombre;
        this.equipo = equipo;
        this.liga = liga;
        this.cotizacionActual = cotizacionActual;
        this.fechaActualizacion = fechaActualizacion;
        this.fuente = fuente;
        this.tokensRestantes = tokensRestantes;
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

    public String getFuente() {
        return fuente;
    }

    public Integer getTokensRestantes() {
        return tokensRestantes;
    }
}
