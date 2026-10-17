package com.prontaentrega.persistence.repository.dao;

import com.prontaentrega.persistence.dto.BolsaDeJugadorDTOSQL;
import com.prontaentrega.models.Liga;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JugadorDAOSQL extends JpaRepository<BolsaDeJugadorDTOSQL, UUID> {

    @EntityGraph(attributePaths = {"estadisticasDeportivas", "estadisticasDeportivas.jugador"})
    @Query("SELECT b FROM BolsaDeJugador b WHERE (:liga IS NULL OR b.estadisticasDeportivas.jugador.liga = :liga) " +
            "AND (:equipo IS NULL OR :equipo = '' OR LOWER(b.estadisticasDeportivas.jugador.equipo) LIKE LOWER(CONCAT('%', :equipo, '%'))) " +
            "AND (:nombre IS NULL OR :nombre = '' OR LOWER(b.estadisticasDeportivas.jugador.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) ")
    Page<BolsaDeJugadorDTOSQL> search(@Param("liga") Liga liga,
                               @Param("equipo") String equipo,
                               @Param("nombre") String nombre,
                               Pageable pageable);

    @EntityGraph(attributePaths = {"estadisticasDeportivas", "estadisticasDeportivas.jugador"})
    @Query("SELECT b FROM BolsaDeJugador b WHERE b.estadisticasDeportivas.jugador.whoscoredId = :whoscoredId")
    Optional<BolsaDeJugadorDTOSQL> findBolsaByWhoscoredId(
            @Param("whoscoredId") Integer whoscoredId);
}
