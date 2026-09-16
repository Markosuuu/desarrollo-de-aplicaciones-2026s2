package com.prontaentrega.services.dto;

import com.prontaentrega.controllers.dtos.PlayerResponse;
import java.util.List;

public record CatalogResponse(List<PlayerResponse> jugadores, Paginacion paginacion) {
    public record Paginacion(int pagina, int porPagina, long total, int totalPaginas) {
    }
}
