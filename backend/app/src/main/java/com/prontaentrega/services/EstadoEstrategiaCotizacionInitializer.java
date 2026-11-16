package com.prontaentrega.services;

import com.prontaentrega.persistence.dto.cotizacion.EstadoEstrategiaCotizacionDTOSQL;
import com.prontaentrega.persistence.repository.dao.EstadoEstrategiaCotizacionDAOSQL;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Asegura la existencia del registro de coordinación sin seleccionar una estrategia. */
@Component
public class EstadoEstrategiaCotizacionInitializer implements ApplicationRunner {
    private final EstadoEstrategiaCotizacionDAOSQL estadoDAO;

    /** Construye el inicializador con el DAO del estado global. */
    public EstadoEstrategiaCotizacionInitializer(EstadoEstrategiaCotizacionDAOSQL estadoDAO) {
        this.estadoDAO = estadoDAO;
    }

    /** Crea el singleton cuando falta y deja la selección activa vacía. */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!estadoDAO.existsById(EstadoEstrategiaCotizacionDTOSQL.ID_GLOBAL)) {
            estadoDAO.save(EstadoEstrategiaCotizacionDTOSQL.estadoInicial());
        }
    }
}