package com.prontaentrega.persistence.repository.dao;

import com.prontaentrega.persistence.dto.OfertaDTOSQL;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OfertaDAOSQL extends JpaRepository<OfertaDTOSQL, UUID> {
    Optional<OfertaDTOSQL> findByUsuario_IdAndSubasta_Id(UUID usuarioId, UUID subastaId);
    List<OfertaDTOSQL> findBySubasta_Id(UUID subastaId);
    List<OfertaDTOSQL> findBySubasta_IdOrderByPujaDesc(UUID subastaId);

    @Modifying
    @Query("UPDATE Oferta o SET o.esSuperior = false WHERE o.subasta.id = :subastaId AND o.esSuperior = true")
    void actualizarSuperior(@Param("subastaId") UUID subastaId);
}
