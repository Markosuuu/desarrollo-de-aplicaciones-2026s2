package com.prontaentrega.services.scraping.whoScoredScrapper;

import com.prontaentrega.models.Jugador;
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
    public Jugador toJugador(PlayerStat stat, LocalDateTime fecha) {
        return Jugador.desdeEstadisticas(
                stat.playerId(),
                stat.name(),
                stat.teamName(),
                stat.tournamentName(),
                stat.age(),
                stat.height(),
                stat.weight(),
                stat.positionText(),
                stat.isActive(),
                stat.goal(),
                stat.assistTotal(),
                stat.shotsPerGame(),
                stat.keyPassPerGame(),
                stat.dribbleWonPerGame(),
                stat.foulGivenPerGame(),
                stat.rating(),
                stat.minsPlayed(),
                fecha
        );
    }

    /**
     * Actualiza un jugador existente con los datos provenientes de WhoScored.
     */
    public void updateJugador(Jugador jugador, PlayerStat stat, LocalDateTime fecha) {
        jugador.actualizarEstadisticas(
                stat.age(),
                stat.height(),
                stat.weight(),
                stat.positionText(),
                stat.isActive(),
                stat.goal(),
                stat.assistTotal(),
                stat.shotsPerGame(),
                stat.keyPassPerGame(),
                stat.dribbleWonPerGame(),
                stat.foulGivenPerGame(),
                stat.rating(),
                stat.minsPlayed(),
                fecha,
                "WhoScored"
        );
    }
}