package com.prontaentrega.controllers.dtos;

import java.util.List;

/**
 * DTO HTTP para respuestas paginadas de jugadores en la capa de controllers.
 */
public record CatalogResponse(List<PlayerResponse> jugadores, Paginacion paginacion) {
    public record Paginacion(int pagina, int porPagina, long total, int totalPaginas) {
    }
}
