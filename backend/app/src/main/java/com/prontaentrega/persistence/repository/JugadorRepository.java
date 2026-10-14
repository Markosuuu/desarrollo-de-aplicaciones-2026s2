package com.prontaentrega.persistence.repository;

import com.prontaentrega.models.Jugador;
import com.prontaentrega.models.Liga;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface JugadorRepository {
    Jugador save(Jugador jugador);
    Page<Jugador> search(Liga liga, String equipo, String nombre, Pageable pageable);
    Optional<Jugador> findByWhoscoredId(Integer whoscoredId);
    void deleteAll();
    Long count();
}
