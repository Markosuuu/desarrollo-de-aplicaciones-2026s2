package com.prontaentrega.persistence.repository.impl;

import com.prontaentrega.models.Operacion;
import com.prontaentrega.persistence.dto.OperacionDTOSQL;
import com.prontaentrega.persistence.repository.OperacionRepository;
import com.prontaentrega.persistence.repository.dao.OperacionDAOSQL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class OperacionRepositoryImpl implements OperacionRepository {
    private final OperacionDAOSQL dao;

    public OperacionRepositoryImpl(OperacionDAOSQL dao) {
        this.dao = dao;
    }

    @Override
    public Operacion save(Operacion operacion) {
        OperacionDTOSQL dto = OperacionDTOSQL.desdeModelo(operacion);
        OperacionDTOSQL saved = dao.save(dto);
        operacion.setId(saved.getId());
        return operacion;
    }

    @Override
    public List<Operacion> findByUsuarioId(UUID usuarioId) {
        return dao.findAllByUsuarioIdOrderByCreadaEnDesc(usuarioId).stream()
                .map(OperacionDTOSQL::aModelo)
                .toList();
    }

    @Override
    public List<Operacion> findByUsuarioIdOrContraparteId(UUID usuarioId) {
        List<Operacion> resultados = new ArrayList<>();
        resultados.addAll(dao.findAllByUsuarioIdOrderByCreadaEnDesc(usuarioId).stream()
                .map(OperacionDTOSQL::aModelo)
                .toList());
        resultados.addAll(dao.findAllByContraparteIdOrderByCreadaEnDesc(usuarioId).stream()
                .map(OperacionDTOSQL::aModelo)
                .toList());
        return resultados.stream().distinct().sorted((a, b) -> b.getCreadaEn().compareTo(a.getCreadaEn())).toList();
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
