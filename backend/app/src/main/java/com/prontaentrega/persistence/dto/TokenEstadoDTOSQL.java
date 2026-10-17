package com.prontaentrega.persistence.dto;

import com.prontaentrega.models.TokenEstado;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "TokenEstado")
@Table(name = "token_estado")
@Getter
@Setter
@NoArgsConstructor
public class TokenEstadoDTOSQL {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false, unique = true)
    private UUID usuarioId;

    @Column(name = "jti_vigente", nullable = false)
    private UUID jtiVigente;

    @Column(name = "version_token", nullable = false)
    private Integer versionToken;

    @Column(name = "emitida_en", nullable = false)
    private LocalDateTime emitidaEn;

    @Column(name = "expira_en", nullable = false)
    private LocalDateTime expiraEn;

    public static TokenEstadoDTOSQL desdeToken(TokenEstado tokenEstado) {
        TokenEstadoDTOSQL dto = new TokenEstadoDTOSQL();
        dto.setId(tokenEstado.getId());
        dto.setUsuarioId(tokenEstado.getUsuarioId());
        dto.setJtiVigente(tokenEstado.getJtiVigente());
        dto.setVersionToken(tokenEstado.getVersionToken());
        dto.setEmitidaEn(tokenEstado.getEmitidaEn());
        dto.setExpiraEn(tokenEstado.getExpiraEn());
        return dto;
    }

    public static TokenEstado aToken(TokenEstadoDTOSQL dto) {
        TokenEstado tokenEstado = new TokenEstado();
        tokenEstado.setId(dto.getId());
        tokenEstado.setUsuarioId(dto.getUsuarioId());
        tokenEstado.setJtiVigente(dto.getJtiVigente());
        tokenEstado.setVersionToken(dto.getVersionToken());
        tokenEstado.setEmitidaEn(dto.getEmitidaEn());
        tokenEstado.setExpiraEn(dto.getExpiraEn());
        return tokenEstado;
    }
}
