package com.prontaentrega.persistence.repository.impl;

import com.prontaentrega.models.Subasta;
import com.prontaentrega.persistence.dto.BolsaDeJugadorDTOSQL;
import com.prontaentrega.persistence.dto.OfertaDTOSQL;
import com.prontaentrega.persistence.dto.SubastaDTOSQL;
import com.prontaentrega.persistence.dto.UsuarioDTOSQL;
import com.prontaentrega.persistence.repository.SubastaRepository;
import com.prontaentrega.persistence.repository.dao.JugadorDAOSQL;
import com.prontaentrega.persistence.repository.dao.OfertaDAOSQL;
import com.prontaentrega.persistence.repository.dao.SubastaDAOSQL;
import com.prontaentrega.persistence.repository.dao.UsuarioDAOSQL;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public class SubastaRepositoryImpl implements SubastaRepository {
    private final SubastaDAOSQL dao;
    private final UsuarioDAOSQL usuarioDao;
    private final JugadorDAOSQL jugadorDao;
    private final OfertaDAOSQL ofertaDao;

    public SubastaRepositoryImpl(
            SubastaDAOSQL dao,
            UsuarioDAOSQL usuarioDao,
            JugadorDAOSQL jugadorDao,
            OfertaDAOSQL ofertaDao) {
        this.dao = dao;
        this.usuarioDao = usuarioDao;
        this.jugadorDao = jugadorDao;
        this.ofertaDao = ofertaDao;
    }

    @Override
    public Subasta save(Subasta subasta) {
        UsuarioDTOSQL propietario = usuarioDao.getReferenceById(subasta.getPropietario().getId());
        BolsaDeJugadorDTOSQL bolsa = jugadorDao.getReferenceById(subasta.getBolsaDeJugador().getId());
        OfertaDTOSQL ofertaActual = subasta.getOfertaActual() == null
                ? null
                : ofertaDao.getReferenceById(subasta.getOfertaActual().getId());
        SubastaDTOSQL dto = generarDTOSubasta(subasta, propietario, bolsa, ofertaActual);
        SubastaDTOSQL saved = dao.save(dto);
        subasta.setId(saved.getId());
        return subasta;
    }

    @NonNull
    private SubastaDTOSQL generarDTOSubasta(Subasta subasta, UsuarioDTOSQL propietario, BolsaDeJugadorDTOSQL bolsa, OfertaDTOSQL ofertaActual) {
        SubastaDTOSQL dto;
        if (subasta.getId() == null) {
            dto = SubastaDTOSQL.desdeModelo(subasta, propietario, bolsa, ofertaActual);
        } else {
            dto = dao.findById(subasta.getId()).orElseGet(SubastaDTOSQL::new);
            dto.setId(subasta.getId());
            dto.setPropietario(propietario);
            dto.setBolsaDeJugador(bolsa);
            dto.setCantidadOfrecida(subasta.getCantidadOfrecida());
            dto.setPrecioInicial(subasta.getPrecioInicial());
            dto.setOfertaActual(ofertaActual);
            dto.setActiva(subasta.getActiva());
            dto.setVencimiento(subasta.getVencimiento());
            dto.setCreadoEn(subasta.getCreadoEn());
            dto.setActualizadoEn(subasta.getActualizadoEn());
            dto.setMotivoCierre(subasta.getMotivoCierre());
        }
        return dto;
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
    public Page<Subasta> findByActiva(Boolean activa, Pageable pageable) {
        return dao.findByActiva(activa, pageable).map(SubastaDTOSQL::aModelo);
    }

    @Override
    public List<UUID> findIdsVencidas(LocalDateTime fecha) {
        return dao.findIdsVencidas(fecha);
    }

    @Override
    public void deleteAll() {
        dao.deleteAll();
    }

    @Override
    public Long count() {
        return dao.count();
    }

    @Override
    public Page<Subasta> findByNombre(String user, String jugador, Pageable pageable) {
        return dao.findAllByUsuarioOJugador(user, jugador, pageable).map(SubastaDTOSQL::aModelo);
    }
}
