package com.prontaentrega.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "token_estado")
public class TokenEstado {
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

    public TokenEstado() {
    }

    public TokenEstado(UUID usuarioId, UUID jtiVigente, Integer versionToken, LocalDateTime emitidaEn, LocalDateTime expiraEn) {
        this.usuarioId = usuarioId;
        this.jtiVigente = jtiVigente;
        this.versionToken = versionToken;
        this.emitidaEn = emitidaEn;
        this.expiraEn = expiraEn;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public UUID getJtiVigente() {
        return jtiVigente;
    }

    public void setJtiVigente(UUID jtiVigente) {
        this.jtiVigente = jtiVigente;
    }

    public Integer getVersionToken() {
        return versionToken;
    }

    public void setVersionToken(Integer versionToken) {
        this.versionToken = versionToken;
    }

    public LocalDateTime getEmitidaEn() {
        return emitidaEn;
    }

    public void setEmitidaEn(LocalDateTime emitidaEn) {
        this.emitidaEn = emitidaEn;
    }

    public LocalDateTime getExpiraEn() {
        return expiraEn;
    }

    public void setExpiraEn(LocalDateTime expiraEn) {
        this.expiraEn = expiraEn;
    }
}
