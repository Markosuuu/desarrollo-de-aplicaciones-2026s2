package com.prontaentrega.persistence.repository.impl;

import com.prontaentrega.models.DatosValoracion;
import com.prontaentrega.persistence.repository.QuoteRecalculationRepository;
import com.prontaentrega.persistence.repository.QuoteRecalculationRepository.ActualizacionCotizacion;
import com.prontaentrega.persistence.repository.dao.QuoteRecalculationDAOSQL;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/** Adapta el DAO SQL al contrato de persistencia del recálculo. */
@Repository
public class QuoteRecalculationRepositoryImpl implements QuoteRecalculationRepository {
    private final QuoteRecalculationDAOSQL dao;

    /** Construye el repositorio con el DAO keyset y batch. */
    public QuoteRecalculationRepositoryImpl(QuoteRecalculationDAOSQL dao) {
        this.dao = dao;
    }

    /** Busca una pagina*/
    @Override
    public List<DatosValoracion> buscarPagina(UUID cursorExclusivo, int limite) {
        return dao.buscarPagina(cursorExclusivo, limite);
    }

    /** Actualiza en un lote las cotizaciones de bolsa */
    @Override
    public int actualizarCotizaciones(List<ActualizacionCotizacion> actualizaciones) {
        return dao.actualizarCotizaciones(actualizaciones);
    }
}