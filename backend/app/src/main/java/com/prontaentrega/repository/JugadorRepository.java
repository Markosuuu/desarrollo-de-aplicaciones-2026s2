package com.prontaentrega.repository;

import com.prontaentrega.models.Jugador;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JugadorRepository extends JpaRepository<Jugador, UUID> {
    @Query("SELECT j FROM Jugador j WHERE (:liga IS NULL OR :liga = '' OR j.liga = :liga) " +
            "AND (:equipo IS NULL OR :equipo = '' OR LOWER(j.equipo) LIKE LOWER(CONCAT('%', :equipo, '%'))) " +
            "AND (:nombre IS NULL OR :nombre = '' OR LOWER(j.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) ")
    Page<Jugador> search(@Param("liga") String liga,
                         @Param("equipo") String equipo,
                         @Param("nombre") String nombre,
                         Pageable pageable);
}
