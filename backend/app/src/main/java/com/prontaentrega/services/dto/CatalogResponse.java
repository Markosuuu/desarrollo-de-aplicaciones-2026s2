package com.prontaentrega.services.dto;

import com.prontaentrega.controllers.dtos.PlayerResponse;
import java.util.List;

/**
 * Respuesta paginada para consultas del catalogo local de jugadores.
 */
public record CatalogResponse(List<PlayerResponse> jugadores, Paginacion paginacion) {
    /**
     * Metadatos de paginacion devueltos junto con los jugadores.
     */
    public record Paginacion(int pagina, int porPagina, long total, int totalPaginas) {
    }
}
