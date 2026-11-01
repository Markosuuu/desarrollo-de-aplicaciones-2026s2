package com.prontaentrega.persistence.repository.impl;

import com.prontaentrega.models.ConfiguracionEstrategiaCotizacion;
import com.prontaentrega.models.estrategias.ConfiguracionEstrategiaPonderadaPenalizacion;
import com.prontaentrega.persistence.dto.ConfiguracionEstrategiaPonderadaPenalizacionDTOSQL;
import com.prontaentrega.persistence.repository.ConfiguracionEstrategiaProvider;
import com.prontaentrega.persistence.repository.dao.ConfiguracionEstrategiaPonderadaPenalizacionDAOSQL;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Provee lectura y escritura de la configuración penalizada tipada. */
@Component
public class ConfiguracionPonderadaPenalizacionProvider implements ConfiguracionEstrategiaProvider {
    private final ConfiguracionEstrategiaPonderadaPenalizacionDAOSQL dao;

    /** Construye el proveedor con el DAO de su configuración exclusiva. */
    public ConfiguracionPonderadaPenalizacionProvider(
            ConfiguracionEstrategiaPonderadaPenalizacionDAOSQL dao) {
        this.dao = dao;
    }

    @Override
    public String estrategiaId() {
        return "ponderada-con-penalizacion";
    }

    @Override
    public Optional<ConfiguracionEstrategiaCotizacion> buscar() {
        return dao.findById(estrategiaId())
                .map(ConfiguracionEstrategiaPonderadaPenalizacionDTOSQL::aModelo);
    }

    @Override
    public Class<ConfiguracionEstrategiaPonderadaPenalizacion> tipoConfiguracion() {
        return ConfiguracionEstrategiaPonderadaPenalizacion.class;
    }

    @Override
    public void guardar(ConfiguracionEstrategiaCotizacion configuracion) {
        if (!(configuracion instanceof ConfiguracionEstrategiaPonderadaPenalizacion penalizada)) {
            throw new IllegalArgumentException(
                    "Tipo de configuración incorrecto para estrategia ponderada con penalización");
        }
        dao.save(ConfiguracionEstrategiaPonderadaPenalizacionDTOSQL.desdeModelo(penalizada));
    }
}