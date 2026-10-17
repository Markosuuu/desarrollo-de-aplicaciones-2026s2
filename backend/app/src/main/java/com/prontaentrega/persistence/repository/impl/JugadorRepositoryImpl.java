package com.prontaentrega.persistence.repository.impl;

import com.prontaentrega.models.BolsaDeJugador;
import com.prontaentrega.models.Liga;
import com.prontaentrega.persistence.dto.BolsaDeJugadorDTOSQL;
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
     * Persiste la bolsa y sus componentes asociados a traves de los DTO relacionales.
     * @param bolsa Informacion del jugador a persistir
     * @return La bolsa persistida con los identificadores asignados
     */
    @Override
    public BolsaDeJugador save(BolsaDeJugador bolsa) {
        BolsaDeJugadorDTOSQL bolsaDTO = BolsaDeJugadorDTOSQL.desdeModelo(bolsa);
        BolsaDeJugador persistida = daosql.save(bolsaDTO).aModelo();
        bolsa.setId(persistida.getId());
        bolsa.getEstadisticasDeportivas().setId(
                persistida.getEstadisticasDeportivas().getId());
        bolsa.getEstadisticasDeportivas().getJugador().setId(
                persistida.getEstadisticasDeportivas().getJugador().getId());
        return bolsa;
    }

    /**
     * Busca bolsas de jugadores en la bbdd relacional
     *
     * @param liga Liga a la que pertenece el jugador
     * @param equipo Equipo al que pertenece el jugador
     * @param nombre Nombre del jugador
     * @param pageable Paginación de resultados
     * @return Una página con las bolsas encontradas
     */
    @Override
    public Page<BolsaDeJugador> search(Liga liga, String equipo, String nombre, Pageable pageable) {
        Page<BolsaDeJugadorDTOSQL> pageDTO = daosql.search(liga, equipo, nombre, pageable);
        return pageDTO.map(BolsaDeJugadorDTOSQL::aModelo);
    }

    /**
     * Busca un jugador por su id de WhoScored en la bbdd relacional
     * @param whoscoredId Id de WhoScored del jugador
     * @return Optional con el jugador encontrado o vacío si no se encuentra
     */
    @Override
    public Optional<BolsaDeJugador> findByWhoscoredId(Integer whoscoredId) {
        return daosql.findBolsaByWhoscoredId(whoscoredId)
                .map(BolsaDeJugadorDTOSQL::aModelo);
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
