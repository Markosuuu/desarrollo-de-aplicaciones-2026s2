package com.prontaentrega.persistence.repository;

import com.prontaentrega.models.BolsaDeJugador;
import com.prontaentrega.models.Liga;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;


public interface JugadorRepository {
    BolsaDeJugador save(BolsaDeJugador bolsa);
    BolsaDeJugador findById(UUID id);
    Page<BolsaDeJugador> search(Liga liga, String equipo, String nombre, Pageable pageable);
    Optional<BolsaDeJugador> findByWhoscoredId(Integer whoscoredId);
    void deleteAll();
    Long count();
}
