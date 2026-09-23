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

/**
 * Registra el resultado de un intento de actualizacion del catalogo.
 */
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

    /**
     * Constructor requerido por JPA.
     */
    public CatalogSnapshot() {
    }

    /**
     * Construye un registro de auditoria para una ejecucion del proceso.
     */
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

    /**
     * Devuelve el identificador tecnico del snapshot.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Devuelve la fecha de inicio del proceso.
     */
    public LocalDateTime getIniciadoEn() {
        return iniciadoEn;
    }

    /**
     * Devuelve la fecha de finalizacion del proceso.
     */
    public LocalDateTime getFinalizadoEn() {
        return finalizadoEn;
    }

    /**
     * Devuelve el estado final registrado.
     */
    public EstadoCatalogo getEstado() {
        return estado;
    }

    /**
     * Devuelve el detalle de error cuando el proceso fallo.
     */
    public String getError() {
        return error;
    }
}
