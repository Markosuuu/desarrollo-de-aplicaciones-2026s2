package com.prontaentrega.persistence.repository.dao;

import com.prontaentrega.persistence.dto.OperacionDTOSQL;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperacionDAOSQL extends JpaRepository<OperacionDTOSQL, UUID> {
    List<OperacionDTOSQL> findAllByUsuario_IdOrderByCreadaEnDesc(UUID usuarioId);

    List<OperacionDTOSQL> findAllByContraparte_IdOrderByCreadaEnDesc(UUID contraparteId);
}
