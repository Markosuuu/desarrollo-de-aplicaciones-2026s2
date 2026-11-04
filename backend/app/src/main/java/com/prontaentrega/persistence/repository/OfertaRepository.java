package com.prontaentrega.persistence.repository;

import com.prontaentrega.models.Oferta;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OfertaRepository {
    Oferta save(Oferta oferta);
    Optional<Oferta> findById(UUID id);
    Optional<Oferta> findByUsuarioIdAndSubastaId(UUID usuarioId, UUID subastaId);
    List<Oferta> findBySubastaId(UUID subastaId);
    List<Oferta> findBySubastaIdOrderByPujaDesc(UUID subastaId);
    void deleteAll();
    Long count();
}
