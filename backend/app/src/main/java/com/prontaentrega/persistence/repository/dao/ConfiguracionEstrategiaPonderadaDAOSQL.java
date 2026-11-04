package com.prontaentrega.persistence.repository.dao;

import com.prontaentrega.persistence.dto.ConfiguracionEstrategiaPonderadaDTOSQL;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso relacional a la configuración ponderada. */
public interface ConfiguracionEstrategiaPonderadaDAOSQL
        extends JpaRepository<ConfiguracionEstrategiaPonderadaDTOSQL, String> {
}