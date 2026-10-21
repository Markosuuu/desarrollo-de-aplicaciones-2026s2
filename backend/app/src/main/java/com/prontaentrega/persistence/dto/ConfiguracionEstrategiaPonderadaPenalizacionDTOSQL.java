package com.prontaentrega.persistence.dto;

import com.prontaentrega.models.ConfiguracionEstrategiaPonderadaPenalizacion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Registro relacional de la configuración de la estrategia con penalización. */
@Entity
@Table(name = "configuracion_estrategia_ponderada_penalizacion")
@Getter
@Setter
@NoArgsConstructor
public class ConfiguracionEstrategiaPonderadaPenalizacionDTOSQL {
    @Id
    @Column(name = "estrategia_id", nullable = false, updatable = false)
    private String estrategiaId = "ponderada-con-penalizacion";

    @Column(name = "peso_goles", nullable = false, precision = 18, scale = 8)
    private BigDecimal pesoGoles;

    @Column(name = "peso_asistencias", nullable = false, precision = 18, scale = 8)
    private BigDecimal pesoAsistencias;

    @Column(name = "peso_disparos_por_partido", nullable = false, precision = 18, scale = 8)
    private BigDecimal pesoDisparosPorPartido;

    @Column(name = "peso_rating", nullable = false, precision = 18, scale = 8)
    private BigDecimal pesoRating;

    @Column(name = "peso_faltas_cometidas_por_partido", nullable = false, precision = 18, scale = 8)
    private BigDecimal pesoFaltasCometidasPorPartido;

    @Column(name = "umbral_regates_ganados_por_partido", nullable = false, precision = 18, scale = 8)
    private BigDecimal umbralRegatesGanadosPorPartido;

    @Column(name = "factor_escala", nullable = false, precision = 18, scale = 8)
    private BigDecimal factorEscala;

    /** Convierte la configuración de dominio a su forma relacional. */
    public static ConfiguracionEstrategiaPonderadaPenalizacionDTOSQL desdeModelo(
            ConfiguracionEstrategiaPonderadaPenalizacion modelo) {
        ConfiguracionEstrategiaPonderadaPenalizacionDTOSQL entidad =
                new ConfiguracionEstrategiaPonderadaPenalizacionDTOSQL();
        entidad.pesoGoles = modelo.pesoGoles();
        entidad.pesoAsistencias = modelo.pesoAsistencias();
        entidad.pesoDisparosPorPartido = modelo.pesoDisparosPorPartido();
        entidad.pesoRating = modelo.pesoRating();
        entidad.pesoFaltasCometidasPorPartido = modelo.pesoFaltasCometidasPorPartido();
        entidad.umbralRegatesGanadosPorPartido = modelo.umbralRegatesGanadosPorPartido();
        entidad.factorEscala = modelo.factorEscala();
        return entidad;
    }

    /** Convierte el registro relacional a una configuración de dominio inmutable. */
    public ConfiguracionEstrategiaPonderadaPenalizacion aModelo() {
        return new ConfiguracionEstrategiaPonderadaPenalizacion(pesoGoles, pesoAsistencias,
                pesoDisparosPorPartido, pesoRating, pesoFaltasCometidasPorPartido,
                umbralRegatesGanadosPorPartido, factorEscala);
    }
}