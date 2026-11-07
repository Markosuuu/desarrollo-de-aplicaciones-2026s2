package com.prontaentrega.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class Usuario {
    private UUID id;
    private final String nombre;
    private final String correo;
    private final String passwordHash;
    private final LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
    private BigDecimal creditosDisponibles;

    public Usuario(String nombre, String correo, String passwordHash, LocalDateTime creadoEn) {
        this.nombre = nombre;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.creadoEn = creadoEn;
        this.actualizadoEn = this.creadoEn;
        this.creditosDisponibles = BigDecimal.valueOf(100);
    }

    public Usuario(String nombre, String correo, String passwordHash) {
        this.nombre = nombre;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.creadoEn = LocalDateTime.now(ZoneId.systemDefault());
        this.actualizadoEn = this.creadoEn;
        this.creditosDisponibles = BigDecimal.valueOf(100);
    }

    public void restarCreditos(BigDecimal precioTotal) {
        this.creditosDisponibles = this.creditosDisponibles.subtract(precioTotal);
    }

    public void sumarCreditos(BigDecimal precioTotal) {
        this.creditosDisponibles = this.creditosDisponibles.add(precioTotal);
    }
}
