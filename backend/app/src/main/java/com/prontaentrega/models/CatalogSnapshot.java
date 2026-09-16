package com.prontaentrega.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "catalog_snapshots")
public class CatalogSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "iniciado_en", nullable = false)
    private LocalDateTime iniciadoEn;

    @Column(name = "finalizado_en")
    private LocalDateTime finalizadoEn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCatalogo estado;

    @Column
    private String error;

    public CatalogSnapshot() {
    }

    public CatalogSnapshot(LocalDateTime iniciadoEn, LocalDateTime finalizadoEn, EstadoCatalogo estado, String error) {
        this.iniciadoEn = iniciadoEn;
        this.finalizadoEn = finalizadoEn;
        this.estado = estado;
        this.error = error;
    }

    public enum EstadoCatalogo {
        VALIDO,
        FALLIDO
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getIniciadoEn() {
        return iniciadoEn;
    }

    public LocalDateTime getFinalizadoEn() {
        return finalizadoEn;
    }

    public EstadoCatalogo getEstado() {
        return estado;
    }

    public String getError() {
        return error;
    }
}
