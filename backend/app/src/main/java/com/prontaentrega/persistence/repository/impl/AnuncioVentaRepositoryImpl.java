package com.prontaentrega.persistence.repository.impl;

import com.prontaentrega.models.AnuncioVenta;
import com.prontaentrega.models.enums.EstadoAnuncioVenta;
import com.prontaentrega.persistence.dto.AnuncioVentaDTOSQL;
import com.prontaentrega.persistence.dto.BolsaDeJugadorDTOSQL;
import com.prontaentrega.persistence.dto.UsuarioDTOSQL;
import com.prontaentrega.persistence.repository.AnuncioVentaRepository;
import com.prontaentrega.persistence.repository.dao.AnuncioVentaDAOSQL;
import com.prontaentrega.persistence.repository.dao.JugadorDAOSQL;
import com.prontaentrega.persistence.repository.dao.UsuarioDAOSQL;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class AnuncioVentaRepositoryImpl implements AnuncioVentaRepository {
    private final AnuncioVentaDAOSQL dao;
    private final UsuarioDAOSQL usuarioDao;
    private final JugadorDAOSQL jugadorDao;

    public AnuncioVentaRepositoryImpl(
            AnuncioVentaDAOSQL dao,
            UsuarioDAOSQL usuarioDao,
            JugadorDAOSQL jugadorDao) {
        this.dao = dao;
        this.usuarioDao = usuarioDao;
        this.jugadorDao = jugadorDao;
    }

    @Override
    public AnuncioVenta save(AnuncioVenta anuncio) {
        UsuarioDTOSQL vendedor = usuarioDao.getReferenceById(anuncio.getVendedor().getId());
        BolsaDeJugadorDTOSQL bolsaDeJugador =
                jugadorDao.getReferenceById(anuncio.getBolsaDeJugador().getId());
        AnuncioVentaDTOSQL dto =
                AnuncioVentaDTOSQL.desdeModelo(anuncio, vendedor, bolsaDeJugador);
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
    public void deleteAll() {
        dao.deleteAll();
    }
}
