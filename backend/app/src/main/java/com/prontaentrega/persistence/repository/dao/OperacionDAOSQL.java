package com.prontaentrega.persistence.repository.dao;

import com.prontaentrega.persistence.dto.OperacionDTOSQL;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperacionDAOSQL extends JpaRepository<OperacionDTOSQL, UUID> {
    List<OperacionDTOSQL> findAllByUsuarioIdOrderByCreadaEnDesc(UUID usuarioId);

    List<OperacionDTOSQL> findAllByContraparteIdOrderByCreadaEnDesc(UUID contraparteId);
}
