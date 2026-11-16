package com.prontaentrega.models.auth;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import io.jsonwebtoken.Claims;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Clase que representa el estado de un token en el sistema.
 */
@Getter
@Setter
@NoArgsConstructor
public class TokenEstado {
    private UUID id;

    private UUID usuarioId;

    private UUID jtiVigente;

    private Integer versionToken;

    private LocalDateTime emitidaEn;

    private LocalDateTime expiraEn;

    public void actualizarToken(UUID usuarioId, Claims claims) {
        this.setUsuarioId(usuarioId);
        this.setJtiVigente(UUID.fromString(claims.getId()));
        this.setVersionToken((this.getVersionToken() == null ? 0 : this.getVersionToken()) + 1);
        this.setEmitidaEn(LocalDateTime.now(ZoneId.systemDefault()));
        this.setExpiraEn(LocalDateTime.now(ZoneId.systemDefault()).plusMinutes(60));
    }
}
