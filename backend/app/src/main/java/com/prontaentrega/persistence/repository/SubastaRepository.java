package com.prontaentrega.persistence.repository;

import com.prontaentrega.models.Subasta;
import com.prontaentrega.models.enums.EstadoSubasta;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SubastaRepository {
    Subasta save(Subasta subasta);
    Optional<Subasta> findById(UUID id);
    Optional<Subasta> findByIdForUpdate(UUID id);
    Page<Subasta> findByEstado(EstadoSubasta estado, Pageable pageable);
    List<UUID> findIdsVencidas(LocalDateTime fecha, Pageable page);
    void deleteAll();
    Long count();
}
