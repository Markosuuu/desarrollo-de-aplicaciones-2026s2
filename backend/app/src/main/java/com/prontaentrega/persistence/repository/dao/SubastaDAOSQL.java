package com.prontaentrega.persistence.repository.dao;

import com.prontaentrega.persistence.dto.SubastaDTOSQL;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubastaDAOSQL extends JpaRepository<SubastaDTOSQL, UUID> {
    Page<SubastaDTOSQL> findByActiva(Boolean activa, Pageable pageable);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("select s from Subasta s where s.id = :id")
    Optional<SubastaDTOSQL> findByIdForUpdate(@Param("id") UUID id);

    @Query("""
    SELECT s.id
    FROM Subasta s
    WHERE s.activa
      AND s.vencimiento <= :fecha
    ORDER BY s.vencimiento ASC
    """)
    List<UUID> findIdsVencidas(
            @Param("fecha") LocalDateTime fecha
    );

    @Query("""
        SELECT s
        FROM Subasta s
        WHERE s.activa
          AND (
              LOWER(s.propietario.nombre) LIKE LOWER(CONCAT('%', :user, '%'))
              OR LOWER(s.bolsaDeJugador.estadisticasDeportivas.jugador.nombre) LIKE LOWER(CONCAT('%', :jugador, '%'))
          )
        ORDER BY s.vencimiento ASC
    """)
    Page<SubastaDTOSQL> findAllByUsuarioOJugador(@Param("user") String user, @Param("jugador") String jugador, Pageable pageable);
}
