package com.prontaentrega.services.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Respuesta principal de WhoScored usada para obtener la tabla de jugadores.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WhoScoredResponse(
        List<WhoScoredPlayerStat> playerTableStats,
        Paging paging
) {

    public List<WhoScoredPlayerStat> stats() {
        return playerTableStats == null ? List.of() : playerTableStats;
    }

    public record Paging(
            Integer currentPage,
            Integer totalPages,
            Integer resultsPerPage,
            Integer totalResults,
            Integer firstRecordIndex,
            Integer lastRecordIndex
    ) {
    }
}
