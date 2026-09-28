package com.prontaentrega.services.scraping;

import com.prontaentrega.models.Jugador;
import com.prontaentrega.services.dto.external.WhoScoredPlayerStat;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

/**
 * Convierte filas de WhoScored en entidades de dominio del catalogo local.
 */
@Component
public class WhoScoredPlayerMapper {

    /**
     * Crea un jugador nuevo desde una fila de WhoScored.
     */
    public Jugador toJugador(WhoScoredPlayerStat stat, LocalDateTime fecha) {
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
    public void updateJugador(Jugador jugador, WhoScoredPlayerStat stat, LocalDateTime fecha) {
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