package com.prontaentrega.persistence.repository;

import com.prontaentrega.models.Operacion;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OperacionRepository {
    Operacion save(Operacion operacion);
    List<Operacion> findByUsuarioId(UUID usuarioId);
    Page<Operacion> findByUsuarioId(UUID usuarioId, Pageable pageable);
    Long count();
    void deleteAll();
}
