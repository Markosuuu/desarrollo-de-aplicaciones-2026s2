package com.prontaentrega.models;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Usuario {
    private UUID id;

    private String nombre;

    private String correo;

    private String passwordHash;

    // TODO: tiene sentido estos dos?
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    public Usuario(String nombre, String correo, String passwordHash) {
        this.nombre = nombre;
        this.correo = correo;
        this.passwordHash = passwordHash;
    }
}
