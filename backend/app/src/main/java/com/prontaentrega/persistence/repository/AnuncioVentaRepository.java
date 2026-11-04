package com.prontaentrega.persistence.repository;

import com.prontaentrega.models.AnuncioVenta;
import com.prontaentrega.models.enums.EstadoAnuncioVenta;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AnuncioVentaRepository {
    AnuncioVenta save(AnuncioVenta anuncio);
    Optional<AnuncioVenta> findById(UUID id);
    Optional<AnuncioVenta> findByIdForUpdate(UUID id);
    Page<AnuncioVenta> findByEstado(EstadoAnuncioVenta estado, Pageable pageable);
    Long count();
    void deleteAll();
}
