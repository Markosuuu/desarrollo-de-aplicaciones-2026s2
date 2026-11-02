package com.prontaentrega.persistence.repository.impl;

import com.prontaentrega.models.AnuncioVenta;
import com.prontaentrega.models.EstadoAnuncioVenta;
import com.prontaentrega.persistence.dto.AnuncioVentaDTOSQL;
import com.prontaentrega.persistence.repository.AnuncioVentaRepository;
import com.prontaentrega.persistence.repository.dao.AnuncioVentaDAOSQL;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class AnuncioVentaRepositoryImpl implements AnuncioVentaRepository {
    private final AnuncioVentaDAOSQL dao;

    public AnuncioVentaRepositoryImpl(AnuncioVentaDAOSQL dao) {
        this.dao = dao;
    }

    @Override
    public AnuncioVenta save(AnuncioVenta anuncio) {
        AnuncioVentaDTOSQL dto = AnuncioVentaDTOSQL.desdeModelo(anuncio);
        AnuncioVentaDTOSQL saved = dao.save(dto);
        anuncio.setId(saved.getId());
        return anuncio;
    }

    @Override
    public Optional<AnuncioVenta> findById(UUID id) {
        return dao.findById(id).map(AnuncioVentaDTOSQL::aModelo);
    }

    @Override
    public Optional<AnuncioVenta> findByIdForUpdate(UUID id) {
        return dao.findByIdForUpdate(id).map(AnuncioVentaDTOSQL::aModelo);
    }

    @Override
    public Page<AnuncioVenta> findByEstado(EstadoAnuncioVenta estado, Pageable pageable) {
        return dao.findByEstado(estado, pageable).map(AnuncioVentaDTOSQL::aModelo);
    }

    @Override
    public Long count() {
        return dao.count();
    }

    @Override
    public void deleteAll() {
        dao.deleteAll();
    }
}
