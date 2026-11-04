package com.prontaentrega.persistence.repository.impl;

import com.prontaentrega.models.ConfiguracionEstrategiaCotizacion;
import com.prontaentrega.models.estrategias.ConfiguracionEstrategiaPonderada;
import com.prontaentrega.persistence.dto.ConfiguracionEstrategiaPonderadaDTOSQL;
import com.prontaentrega.persistence.repository.ConfiguracionEstrategiaProvider;
import com.prontaentrega.persistence.repository.dao.ConfiguracionEstrategiaPonderadaDAOSQL;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Provee lectura y escritura de la configuración ponderada tipada. */
@Component
public class ConfiguracionPonderadaProvider implements ConfiguracionEstrategiaProvider {
    private final ConfiguracionEstrategiaPonderadaDAOSQL dao;

    /** Construye el proveedor con el DAO de su configuración exclusiva. */
    public ConfiguracionPonderadaProvider(ConfiguracionEstrategiaPonderadaDAOSQL dao) {
        this.dao = dao;
    }

    @Override
    public String estrategiaId() {
        return "ponderada";
    }

    @Override
    public Optional<ConfiguracionEstrategiaCotizacion> buscar() {
        return dao.findById(estrategiaId())
                .map(ConfiguracionEstrategiaPonderadaDTOSQL::aModelo);
    }

    @Override
    public Class<ConfiguracionEstrategiaPonderada> tipoConfiguracion() {
        return ConfiguracionEstrategiaPonderada.class;
    }

    @Override
    public void guardar(ConfiguracionEstrategiaCotizacion configuracion) {
        if (!(configuracion instanceof ConfiguracionEstrategiaPonderada ponderada)) {
            throw new IllegalArgumentException("Tipo de configuración incorrecto para estrategia ponderada");
        }
        dao.save(ConfiguracionEstrategiaPonderadaDTOSQL.desdeModelo(ponderada));
    }
}