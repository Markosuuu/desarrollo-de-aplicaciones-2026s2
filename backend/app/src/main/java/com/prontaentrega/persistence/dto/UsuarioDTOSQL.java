package com.prontaentrega.persistence.dto;

import com.prontaentrega.models.Usuario;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "Usuario")
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class UsuarioDTOSQL {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en", nullable = false)
    @Setter(AccessLevel.NONE)
    private LocalDateTime actualizadoEn;

    public UsuarioDTOSQL(String nombre, String correo, String passwordHash) {
        this.nombre = nombre;
        this.correo = correo;
        this.passwordHash = passwordHash;
    }

    public static UsuarioDTOSQL desdeUsuario(Usuario usuario) {
        return new UsuarioDTOSQL(usuario.getNombre(), usuario.getCorreo(), usuario.getPasswordHash());
    }

    public static Usuario aUsuario(UsuarioDTOSQL usuarioDTOSQL) {
        Usuario usuario = new Usuario();
        usuario.setId(usuarioDTOSQL.getId());
        usuario.setNombre(usuarioDTOSQL.getNombre());
        usuario.setCorreo(usuarioDTOSQL.getCorreo());
        usuario.setPasswordHash(usuarioDTOSQL.getPasswordHash());
        usuario.setCreadoEn(usuarioDTOSQL.getCreadoEn());
        usuario.setActualizadoEn(usuarioDTOSQL.getActualizadoEn());
        return usuario;
    }
}
