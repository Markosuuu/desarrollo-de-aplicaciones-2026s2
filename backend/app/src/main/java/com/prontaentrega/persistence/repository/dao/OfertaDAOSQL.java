package com.prontaentrega.persistence.repository.dao;

import aj.org.objectweb.asm.commons.Remapper;
import com.prontaentrega.persistence.dto.subastas.OfertaDTOSQL;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OfertaDAOSQL extends JpaRepository<OfertaDTOSQL, UUID> {
    Optional<OfertaDTOSQL> findByUsuario_IdAndIdempotencyKey(UUID usuarioId, String idempotencyKey);
    List<OfertaDTOSQL> findBySubasta_Id(UUID subastaId);
    List<OfertaDTOSQL> findBySubasta_IdOrderByPujaDesc(UUID subastaId);

    @Modifying
    @Query("UPDATE Oferta o SET o.esSuperior = false WHERE o.subasta.id = :subastaId AND o.esSuperior = true")
    void actualizarSuperior(@Param("subastaId") UUID subastaId);

    @Query("""
        SELECT o
        FROM Oferta o
        WHERE o.subasta.id = :subastaId
          AND o.usuario.creditosDisponibles >= o.puja
        ORDER BY o.puja DESC, o.creadoEn ASC
        LIMIT 1
    """)
    Optional<OfertaDTOSQL> findOfertaGanadora(@Param("subastaId") UUID subastaId);
}
