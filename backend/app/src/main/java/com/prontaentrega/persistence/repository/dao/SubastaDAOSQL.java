package com.prontaentrega.persistence.repository.dao;

import com.prontaentrega.models.enums.EstadoSubasta;
import com.prontaentrega.persistence.dto.SubastaDTOSQL;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubastaDAOSQL extends JpaRepository<SubastaDTOSQL, UUID> {
    Page<SubastaDTOSQL> findByEstado(EstadoSubasta estado, Pageable pageable);

    List<SubastaDTOSQL> findByEstadoAndVencimientoLessThanEqual(EstadoSubasta estado, LocalDateTime fecha);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("select s from Subasta s where s.id = :id")
    Optional<SubastaDTOSQL> findByIdForUpdate(@Param("id") UUID id);

    @Query("""
    SELECT s.id
    FROM Subasta s
    WHERE s.estado = :estado
      AND s.vencimiento <= :fecha
    ORDER BY s.vencimiento ASC
    """)
    List<UUID> findIdsVencidas(
            @Param("estado") EstadoSubasta estado,
            @Param("fecha") LocalDateTime fecha,
            Pageable pageable
    );
}
