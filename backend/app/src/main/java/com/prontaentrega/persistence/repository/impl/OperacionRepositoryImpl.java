package com.prontaentrega.persistence.repository.impl;

import com.prontaentrega.models.Operacion;
import com.prontaentrega.persistence.dto.OperacionDTOSQL;
import com.prontaentrega.persistence.dto.UsuarioDTOSQL;
import com.prontaentrega.persistence.repository.OperacionRepository;
import com.prontaentrega.persistence.repository.dao.OperacionDAOSQL;
import com.prontaentrega.persistence.repository.dao.JugadorDTOSQLDAO;
import com.prontaentrega.persistence.repository.dao.UsuarioDAOSQL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class OperacionRepositoryImpl implements OperacionRepository {
    private final OperacionDAOSQL dao;
    private final UsuarioDAOSQL usuarioDao;
    private final JugadorDTOSQLDAO jugadorDao;

    public OperacionRepositoryImpl(
            OperacionDAOSQL dao,
            UsuarioDAOSQL usuarioDao,
            JugadorDTOSQLDAO jugadorDao) {
        this.dao = dao;
        this.usuarioDao = usuarioDao;
        this.jugadorDao = jugadorDao;
    }

    @Override
    public Operacion save(Operacion operacion) {
        UsuarioDTOSQL usuario = usuarioDao.getReferenceById(operacion.getUsuario().getId());
        UsuarioDTOSQL contraparte = usuarioDao.getReferenceById(operacion.getContraparte().getId());
        OperacionDTOSQL dto = OperacionDTOSQL.desdeModelo(
                operacion,
                usuario,
                contraparte,
                jugadorDao.getReferenceById(operacion.getJugador().getId()));
        OperacionDTOSQL saved = dao.save(dto);
        operacion.setId(saved.getId());
        return operacion;
    }

    @Override
    public List<Operacion> findByUsuarioId(UUID usuarioId) {
        return dao.findAllByUsuario_IdOrderByCreadaEnDesc(usuarioId).stream()
                .map(OperacionDTOSQL::aModelo)
                .toList();
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
