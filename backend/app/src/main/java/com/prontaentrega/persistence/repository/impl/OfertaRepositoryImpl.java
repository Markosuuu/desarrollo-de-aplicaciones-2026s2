package com.prontaentrega.persistence.repository.impl;

import com.prontaentrega.models.Oferta;
import com.prontaentrega.persistence.dto.OfertaDTOSQL;
import com.prontaentrega.persistence.dto.SubastaDTOSQL;
import com.prontaentrega.persistence.dto.UsuarioDTOSQL;
import com.prontaentrega.persistence.repository.OfertaRepository;
import com.prontaentrega.persistence.repository.dao.OfertaDAOSQL;
import com.prontaentrega.persistence.repository.dao.SubastaDAOSQL;
import com.prontaentrega.persistence.repository.dao.UsuarioDAOSQL;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class OfertaRepositoryImpl implements OfertaRepository {
    private final OfertaDAOSQL dao;
    private final SubastaDAOSQL subastaDao;
    private final UsuarioDAOSQL usuarioDao;

    public OfertaRepositoryImpl(OfertaDAOSQL dao, SubastaDAOSQL subastaDao, UsuarioDAOSQL usuarioDao) {
        this.dao = dao;
        this.subastaDao = subastaDao;
        this.usuarioDao = usuarioDao;
    }

    @Override
    public Oferta save(Oferta oferta) {
        SubastaDTOSQL subasta = subastaDao.getReferenceById(oferta.getSubastaId());
        UsuarioDTOSQL usuario = usuarioDao.getReferenceById(oferta.getUsuarioId());
        OfertaDTOSQL dto = OfertaDTOSQL.desdeModelo(oferta, subasta, usuario);
        OfertaDTOSQL saved = dao.save(dto);
        oferta.setId(saved.getId());
        return oferta;
    }

    @Override
    public Optional<Oferta> findById(UUID id) {
        return dao.findById(id).map(OfertaDTOSQL::aModelo);
    }

    @Override
    public Optional<Oferta> findByUsuarioIdAndIdempotencyKey(UUID usuarioId, String idempotencyKey) {
        return dao.findByUsuario_IdAndIdempotencyKey(usuarioId, idempotencyKey).map(OfertaDTOSQL::aModelo);
    }

    @Override
    public List<Oferta> findBySubastaId(UUID subastaId) {
        return dao.findBySubasta_Id(subastaId).stream().map(OfertaDTOSQL::aModelo).toList();
    }

    @Override
    public List<Oferta> findBySubastaIdOrderByPujaDesc(UUID subastaId) {
        return dao.findBySubasta_IdOrderByPujaDesc(subastaId).stream().map(OfertaDTOSQL::aModelo).toList();
    }

    @Override
    @Transactional
    public void deleteAll() {
        List<SubastaDTOSQL> subastas = subastaDao.findAll();
        subastas.forEach(subasta -> subasta.setOfertaActual(null));
        subastaDao.saveAll(subastas);
        subastaDao.flush();
        dao.deleteAll();
    }

    @Override
    public Long count() {
        return dao.count();
    }

    @Override
    public void actualizarSuperior(UUID subastaId) {
        dao.actualizarSuperior(subastaId);
    }
}
