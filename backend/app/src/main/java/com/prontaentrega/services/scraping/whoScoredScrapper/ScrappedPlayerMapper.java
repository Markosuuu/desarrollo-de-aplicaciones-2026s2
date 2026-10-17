package com.prontaentrega.services.scraping.whoScoredScrapper;

import com.prontaentrega.models.*;
import com.prontaentrega.models.builders.EstadisticasDeportivasDatos;
import com.prontaentrega.services.dto.external.PlayerStat;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

/**
 * Convierte filas de WhoScored en entidades de dominio del catalogo local.
 */
@Component
public class ScrappedPlayerMapper {

    /**
     * Crea un jugador nuevo desde una fila de WhoScored.
     */
    public BolsaDeJugador toBolsaDeJugador(PlayerStat stat, LocalDateTime fecha) {

        Jugador jugador = new Jugador(
                stat.playerId(),
                stat.name(),
                stat.teamName(),
                Liga.fromValue(stat.tournamentName()),
                Posicion.fromValue(stat.positionText()),
                stat.age(),
                stat.height(),
                stat.weight()
        );

        EstadisticasDeportivas estadisticas = new EstadisticasDeportivas(
                jugador,
                stat.isActive(),
                stat.goal(),
                stat.assistTotal(),
                stat.shotsPerGame(),
                stat.keyPassPerGame(),
                stat.dribbleWonPerGame(),
                stat.foulGivenPerGame(),
                stat.rating(),
                stat.minsPlayed()
        );

        return BolsaDeJugador.desdeEstadisticas(estadisticas, fecha);
    }

    /**
     * Actualiza un jugador existente con los datos provenientes de WhoScored.
     */
    public void updateBolsaDeJugador(BolsaDeJugador bolsa, PlayerStat stat, LocalDateTime fecha) {
        EstadisticasDeportivas estadisticas = bolsa.getEstadisticasDeportivas();
        Jugador jugador = estadisticas.getJugador();

        jugador.actualizar(
                stat.age(),
                stat.height(),
                stat.weight(),
                Posicion.fromValue(stat.positionText())
        );

        EstadisticasDeportivasDatos datos = EstadisticasDeportivasDatos.builder()
                .jugador(jugador)
                .activo(stat.isActive())
                .goles(stat.goal())
                .asistencias(stat.assistTotal())
                .disparosPorPartido(stat.shotsPerGame())
                .keyPassesPorPartido(stat.keyPassPerGame())
                .dribblesGanadosPorPartido(stat.dribbleWonPerGame())
                .faltasCometidasPorPartido(stat.foulGivenPerGame())
                .rating(stat.rating())
                .minutosJugados(stat.minsPlayed())
                .build();

        estadisticas.actualizar(datos);

        bolsa.actualizarEstadisticas(estadisticas, fecha, "WhoScored");
    }
}