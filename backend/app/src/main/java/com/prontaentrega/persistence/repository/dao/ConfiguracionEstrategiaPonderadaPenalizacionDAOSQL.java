package com.prontaentrega.persistence.repository.dao;

import com.prontaentrega.persistence.dto.ConfiguracionEstrategiaPonderadaPenalizacionDTOSQL;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso relacional a la configuración ponderada con penalización. */
public interface ConfiguracionEstrategiaPonderadaPenalizacionDAOSQL
        extends JpaRepository<ConfiguracionEstrategiaPonderadaPenalizacionDTOSQL, String> {
}