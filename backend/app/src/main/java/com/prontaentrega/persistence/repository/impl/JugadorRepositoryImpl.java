package com.prontaentrega.persistence.repository.impl;

import com.prontaentrega.models.Jugador;
import com.prontaentrega.persistence.dto.JugadorDTOSQL;
import com.prontaentrega.persistence.repository.JugadorRepository;
import com.prontaentrega.persistence.repository.dao.JugadorDAOSQL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JugadorRepositoryImpl implements JugadorRepository {
    private final JugadorDAOSQL daosql;

    public JugadorRepositoryImpl(JugadorDAOSQL daosql) {
        this.daosql = daosql;
    }

    /**
     * Guarda un jugador en la bbdd relacional
     * Primero mapea eljugador como DTO y luego lo guarda
     * @param jugador Jugador a persistir
     * @return El jugador persistido con su id asignado
     */
    @Override
    public Jugador save(Jugador jugador) {
        JugadorDTOSQL jugadorDTO = JugadorDTOSQL.desdeJugador(jugador);

        JugadorDTOSQL jugadorGuardado = daosql.save(jugadorDTO);

        jugador.setId(jugadorGuardado.getId());

        return jugador;
    }

    /**
     * Busca jugadores en la bbdd relacional
     *
     * @param liga Liga a la que pertenece el jugador
     * @param equipo Equipo al que pertenece el jugador
     * @param nombre Nombre del jugador
     * @param pageable Paginación de resultados
     * @return Una página con los jugadores encontrados
     */
    @Override
    public Page<Jugador> search(String liga, String equipo, String nombre, Pageable pageable) {
        Page<JugadorDTOSQL> pageDTO = daosql.search(liga, equipo, nombre, pageable);

        return pageDTO.map(JugadorDTOSQL::aJugador);
    }

    /**
     * Busca un jugador por su id de WhoScored en la bbdd relacional
     * @param whoscoredId Id de WhoScored del jugador
     * @return Optional con el jugador encontrado o vacío si no se encuentra
     */
    @Override
    public Optional<Jugador> findByWhoscoredId(Integer whoscoredId) {
        return daosql.findJugadorDTOSQLByWhoscoredId(whoscoredId)
                .map(JugadorDTOSQL::aJugador);
    }

    @Override
    public void deleteAll() {
        daosql.deleteAll();
    }

    @Override
    public Long count() {
        return daosql.count();
    }
}
