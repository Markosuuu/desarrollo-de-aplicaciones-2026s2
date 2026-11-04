package com.prontaentrega.persistence.repository;

import com.prontaentrega.models.Operacion;
import java.util.List;
import java.util.UUID;

public interface OperacionRepository {
    Operacion save(Operacion operacion);
    List<Operacion> findByUsuarioId(UUID usuarioId);
    Long count();
    void deleteAll();
}
