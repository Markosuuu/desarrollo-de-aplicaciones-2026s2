package com.prontaentrega.persistence.repository.dao;

import com.prontaentrega.persistence.dto.OperacionDTOSQL;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperacionDAOSQL extends JpaRepository<OperacionDTOSQL, UUID> {
    List<OperacionDTOSQL> findAllByUsuario_IdOrderByCreadaEnDesc(UUID usuarioId);

    Page<OperacionDTOSQL> findAllByUsuario_IdOrderByCreadaEnDesc(UUID usuarioId, Pageable pageable);

    List<OperacionDTOSQL> findAllByContraparte_IdOrderByCreadaEnDesc(UUID contraparteId);
}
