package com.prontaentrega.repository;

import com.prontaentrega.models.CatalogSnapshot;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogSnapshotRepository extends JpaRepository<CatalogSnapshot, UUID> {
    Optional<CatalogSnapshot> findTopByEstadoOrderByIniciadoEnDesc(CatalogSnapshot.EstadoCatalogo estado);
}
