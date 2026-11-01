package com.prontaentrega.persistence.dto;

import com.prontaentrega.models.EstadoEstrategiaCotizacion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Fila singleton que conserva la estrategia activa del mercado. */
@Entity
@Table(name = "estado_estrategia_cotizacion")
@Getter
@Setter
@NoArgsConstructor
public class EstadoEstrategiaCotizacionDTOSQL {
    public static final String ID_GLOBAL = "global";

    @Id
    @Column(nullable = false, updatable = false)
    private String id = ID_GLOBAL;

    @Column(name = "estrategia_activa_id")
    private String estrategiaActivaId;

    /** Crea una fila sin estrategia activa inicial. */
    public static EstadoEstrategiaCotizacionDTOSQL estadoInicial() {
        return new EstadoEstrategiaCotizacionDTOSQL();
    }

    /** Convierte el estado relacional al modelo de dominio. */
    public EstadoEstrategiaCotizacion aModelo() {
        return new EstadoEstrategiaCotizacion(estrategiaActivaId);
    }

    /** Actualiza la selección activa. */
    public void seleccionar(String estrategiaId) {
        this.estrategiaActivaId = estrategiaId;
    }
}