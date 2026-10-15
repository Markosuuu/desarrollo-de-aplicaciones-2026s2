package com.prontaentrega.services.scraping.whoScoredScrapper;

import com.prontaentrega.models.BolsaDeJugador;
import com.prontaentrega.models.Liga;
import com.prontaentrega.models.Posicion;
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
        return BolsaDeJugador.desdeEstadisticas(
                stat.playerId(),
                stat.name(),
                stat.teamName(),
                Liga.fromValue(stat.tournamentName()),
                stat.age(),
                stat.height(),
                stat.weight(),
                Posicion.fromValue(stat.positionText()),
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
    public void updateBolsaDeJugador(BolsaDeJugador bolsa, PlayerStat stat, LocalDateTime fecha) {
        bolsa.actualizarEstadisticas(
                stat.age(),
                stat.height(),
                stat.weight(),
                Posicion.fromValue(stat.positionText()),
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