package com.prontaentrega.persistence.dto;

import com.prontaentrega.models.Usuario;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    @Column(name = "creditos_disponibles", nullable = false)
    private BigDecimal creditosDisponibles = BigDecimal.valueOf(100);

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
        this.creditosDisponibles = BigDecimal.valueOf(100);
    }

    public static UsuarioDTOSQL desdeUsuario(Usuario usuario) {
        UsuarioDTOSQL dto = new UsuarioDTOSQL(usuario.getNombre(), usuario.getCorreo(), usuario.getPasswordHash());
        dto.id = usuario.getId();
        dto.setCreditosDisponibles(usuario.getCreditosDisponibles() == null
                ? BigDecimal.valueOf(100)
                : usuario.getCreditosDisponibles());
        return dto;
    }

    public static Usuario aUsuario(UsuarioDTOSQL usuarioDTOSQL) {
        return Usuario.builder()
                .id(usuarioDTOSQL.getId())
                .nombre(usuarioDTOSQL.getNombre())
                .correo(usuarioDTOSQL.getCorreo())
                .passwordHash(usuarioDTOSQL.getPasswordHash())
                .creditosDisponibles(usuarioDTOSQL.getCreditosDisponibles())
                .creadoEn(usuarioDTOSQL.getCreadoEn())
                .actualizadoEn(usuarioDTOSQL.getActualizadoEn())
                .build();
    }
}
