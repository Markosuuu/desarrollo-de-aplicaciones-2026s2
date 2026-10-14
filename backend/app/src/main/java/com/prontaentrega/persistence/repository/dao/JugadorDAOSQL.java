package com.prontaentrega.persistence.repository.dao;

import com.prontaentrega.persistence.dto.JugadorDTOSQL;
import com.prontaentrega.models.Liga;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JugadorDAOSQL extends JpaRepository<JugadorDTOSQL, UUID> {


    @Query("SELECT j FROM Jugador j WHERE (:liga IS NULL OR j.liga = :liga) " +
            "AND (:equipo IS NULL OR :equipo = '' OR LOWER(j.equipo) LIKE LOWER(CONCAT('%', :equipo, '%'))) " +
            "AND (:nombre IS NULL OR :nombre = '' OR LOWER(j.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) ")
    Page<JugadorDTOSQL> search(@Param("liga") Liga liga,
                               @Param("equipo") String equipo,
                               @Param("nombre") String nombre,
                               Pageable pageable);

    Optional<JugadorDTOSQL> findJugadorDTOSQLByWhoscoredId(Integer whoscoredId);
}
