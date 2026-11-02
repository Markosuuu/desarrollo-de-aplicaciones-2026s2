package com.prontaentrega.persistence.repository.dao;

import com.prontaentrega.models.EstadoAnuncioVenta;
import com.prontaentrega.persistence.dto.AnuncioVentaDTOSQL;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnuncioVentaDAOSQL extends JpaRepository<AnuncioVentaDTOSQL, UUID> {
    Page<AnuncioVentaDTOSQL> findByEstado(EstadoAnuncioVenta estado, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AnuncioVenta a where a.id = :id")
    Optional<AnuncioVentaDTOSQL> findByIdForUpdate(@Param("id") UUID id);
}
