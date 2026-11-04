package com.prontaentrega.persistence.repository.impl;

import com.prontaentrega.models.Subasta;
import com.prontaentrega.models.enums.EstadoSubasta;
import com.prontaentrega.persistence.dto.BolsaDeJugadorDTOSQL;
import com.prontaentrega.persistence.dto.SubastaDTOSQL;
import com.prontaentrega.persistence.dto.UsuarioDTOSQL;
import com.prontaentrega.persistence.repository.SubastaRepository;
import com.prontaentrega.persistence.repository.dao.JugadorDAOSQL;
import com.prontaentrega.persistence.repository.dao.SubastaDAOSQL;
import com.prontaentrega.persistence.repository.dao.UsuarioDAOSQL;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class SubastaRepositoryImpl implements SubastaRepository {
    private final SubastaDAOSQL dao;
    private final UsuarioDAOSQL usuarioDao;
    private final JugadorDAOSQL jugadorDao;

    public SubastaRepositoryImpl(SubastaDAOSQL dao, UsuarioDAOSQL usuarioDao, JugadorDAOSQL jugadorDao) {
        this.dao = dao;
        this.usuarioDao = usuarioDao;
        this.jugadorDao = jugadorDao;
    }

    @Override
    public Subasta save(Subasta subasta) {
        UsuarioDTOSQL propietario = usuarioDao.getReferenceById(subasta.getPropietario().getId());
        BolsaDeJugadorDTOSQL bolsa = jugadorDao.getReferenceById(subasta.getBolsaDeJugador().getId());
        UsuarioDTOSQL ofertante = subasta.getOfertanteActual() == null
                ? null
                : usuarioDao.getReferenceById(subasta.getOfertanteActual().getId());
        SubastaDTOSQL dto;
        if (subasta.getId() == null) {
            dto = SubastaDTOSQL.desdeModelo(subasta, propietario, bolsa, ofertante);
        } else {
            // TODO: CHECK THA SHI
            dto = dao.findById(subasta.getId()).orElseGet(SubastaDTOSQL::new);
            dto.setId(subasta.getId());
            dto.setPropietario(propietario);
            dto.setBolsaDeJugador(bolsa);
            dto.setCantidadOfrecida(subasta.getCantidadOfrecida());
            dto.setPrecioInicial(subasta.getPrecioInicial());
            dto.setOfertaActual(subasta.getOfertaActual());
            dto.setOfertanteActual(ofertante);
            dto.setEstado(subasta.getEstado());
            dto.setVencimiento(subasta.getVencimiento());
            dto.setCreadoEn(subasta.getCreadoEn());
            dto.setActualizadoEn(subasta.getActualizadoEn());
            dto.setMotivoCierre(subasta.getMotivoCierre());
        }
        SubastaDTOSQL saved = dao.save(dto);
        subasta.setId(saved.getId());
        return subasta;
    }

    @Override
    public Optional<Subasta> findById(UUID id) {
        return dao.findById(id).map(SubastaDTOSQL::aModelo);
    }

    @Override
    public Optional<Subasta> findByIdForUpdate(UUID id) {
        return dao.findByIdForUpdate(id).map(SubastaDTOSQL::aModelo);
    }

    @Override
    public Page<Subasta> findByEstado(EstadoSubasta estado, Pageable pageable) {
        return dao.findByEstado(estado, pageable).map(SubastaDTOSQL::aModelo);
    }

    @Override
    public List<Subasta> findVencidas(LocalDateTime fecha) {
        return dao.findByEstadoAndVencimientoLessThanEqual(EstadoSubasta.ACTIVA, fecha).stream()
                .map(SubastaDTOSQL::aModelo)
                .toList();
    }

    @Override
    public void deleteAll() {
        dao.deleteAll();
    }

    @Override
    public Long count() {
        return dao.count();
    }
}
