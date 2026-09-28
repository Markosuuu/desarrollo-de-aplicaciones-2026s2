package com.prontaentrega.services.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

/**
 * Fila de estadisticas de jugador devuelta por WhoScored dentro de playerTableStats.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WhoScoredPlayerStat(
        Integer height,
        Integer weight,
        Integer age,
        Boolean isActive,
        String playedPositionsShort,

        Integer apps,
        Integer goal,
        Integer assistTotal,

        BigDecimal shotsPerGame,
        BigDecimal keyPassPerGame,
        BigDecimal dribbleWonPerGame,
        BigDecimal foulGivenPerGame,

        String name,
        Integer playerId,
        String positionText,
        String teamName,
        String tournamentName,

        BigDecimal rating,
        Integer minsPlayed
) {
}