package com.prontaentrega.persistence.dto;

import com.prontaentrega.models.ConfiguracionEstrategiaPonderada;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Registro relacional de la configuración de la estrategia ponderada. */
@Entity
@Table(name = "configuracion_estrategia_ponderada")
@Getter
@Setter
@NoArgsConstructor
public class ConfiguracionEstrategiaPonderadaDTOSQL {
    @Id
    @Column(name = "estrategia_id", nullable = false, updatable = false)
    private String estrategiaId = "ponderada";

    @Column(name = "peso_goles", nullable = false, precision = 18, scale = 8)
    private BigDecimal pesoGoles;

    @Column(name = "peso_asistencias", nullable = false, precision = 18, scale = 8)
    private BigDecimal pesoAsistencias;

    @Column(name = "peso_disparos_por_partido", nullable = false, precision = 18, scale = 8)
    private BigDecimal pesoDisparosPorPartido;

    @Column(name = "peso_pases_clave_por_partido", nullable = false, precision = 18, scale = 8)
    private BigDecimal pesoPasesClavePorPartido;

    @Column(name = "peso_regates_ganados_por_partido", nullable = false, precision = 18, scale = 8)
    private BigDecimal pesoRegatesGanadosPorPartido;

    @Column(name = "peso_faltas_cometidas_por_partido", nullable = false, precision = 18, scale = 8)
    private BigDecimal pesoFaltasCometidasPorPartido;

    @Column(name = "peso_rating", nullable = false, precision = 18, scale = 8)
    private BigDecimal pesoRating;

    @Column(name = "factor_escala", nullable = false, precision = 18, scale = 8)
    private BigDecimal factorEscala;

    /** Convierte la configuración de dominio a su forma relacional. */
    public static ConfiguracionEstrategiaPonderadaDTOSQL desdeModelo(
            ConfiguracionEstrategiaPonderada modelo) {
        ConfiguracionEstrategiaPonderadaDTOSQL entidad = new ConfiguracionEstrategiaPonderadaDTOSQL();
        entidad.pesoGoles = modelo.pesoGoles();
        entidad.pesoAsistencias = modelo.pesoAsistencias();
        entidad.pesoDisparosPorPartido = modelo.pesoDisparosPorPartido();
        entidad.pesoPasesClavePorPartido = modelo.pesoPasesClavePorPartido();
        entidad.pesoRegatesGanadosPorPartido = modelo.pesoRegatesGanadosPorPartido();
        entidad.pesoFaltasCometidasPorPartido = modelo.pesoFaltasCometidasPorPartido();
        entidad.pesoRating = modelo.pesoRating();
        entidad.factorEscala = modelo.factorEscala();
        return entidad;
    }

    /** Convierte el registro relacional a una configuración de dominio inmutable. */
    public ConfiguracionEstrategiaPonderada aModelo() {
        return new ConfiguracionEstrategiaPonderada(pesoGoles, pesoAsistencias,
                pesoDisparosPorPartido, pesoPasesClavePorPartido,
                pesoRegatesGanadosPorPartido, pesoFaltasCometidasPorPartido,
                pesoRating, factorEscala);
    }
}