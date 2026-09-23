package com.prontaentrega.services;

import com.prontaentrega.controllers.dtos.PlayerResponse;
import com.prontaentrega.models.Jugador;
import com.prontaentrega.repository.CatalogSnapshotRepository;
import com.prontaentrega.repository.JugadorRepository;
import com.prontaentrega.services.dto.CatalogResponse;
import com.prontaentrega.services.dto.RefreshCatalogResponse;
import com.prontaentrega.services.dto.external.WhoScoredPlayerStat;
import com.prontaentrega.services.dto.external.WhoScoredResponse;
import com.prontaentrega.services.exceptions.ProviderUnavailableException;
import com.prontaentrega.services.scraping.WhoScoredClient;
import com.prontaentrega.services.scraping.WhoScoredPlayerMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de catalogo de jugadores.
 * Orquesta lectura local, obtencion de WhoScored y persistencia segura sin
 * ubicar reglas de dominio fuera de la entidad Jugador.
 */
@Service
public class PlayerCatalogService {
    private final JugadorRepository jugadorRepository;
    private final WhoScoredClient whoScoredClient;
    private final WhoScoredPlayerMapper whoScoredPlayerMapper;

    public PlayerCatalogService(JugadorRepository jugadorRepository,
                                CatalogSnapshotRepository catalogSnapshotRepository,
                                WhoScoredClient whoScoredClient,
                                WhoScoredPlayerMapper whoScoredPlayerMapper) {
        this.jugadorRepository = jugadorRepository;
        this.whoScoredClient = whoScoredClient;
        this.whoScoredPlayerMapper = whoScoredPlayerMapper;
    }

    /**
     * Busca jugadores en la base local sin consultar proveedores externos.
     */
    @Transactional(readOnly = true)
    public CatalogResponse search(String liga, String equipo, String nombre, int page, int perPage) {
        int safePage = Math.max(page, 1);
        int safePerPage = Math.max(Math.min(perPage, 50), 1);

        Pageable pageable = PageRequest.of(safePage - 1, safePerPage);
        Page<Jugador> result = jugadorRepository.search(normalize(liga), normalize(equipo), normalize(nombre), pageable);

        List<PlayerResponse> players = result.getContent().stream()
                .map(PlayerResponse::from)
                .toList();

        return new CatalogResponse(players, new CatalogResponse.Paginacion(
                safePage,
                safePerPage,
                result.getTotalElements(),
                result.getTotalPages() == 0 ? 1 : result.getTotalPages()
        ));
    }

    /**
     * Actualiza el catalogo desde WhoScored preservando los datos locales
     * ante fallos del proveedor.
     */
    @Transactional
    public RefreshCatalogResponse refreshFromWhoScored() {
        LocalDateTime updatedAt = LocalDateTime.now();

        final int pageSize = 20;

        // 1. Descargar todas las paginas antes de modificar la base.
        WhoScoredResponse firstPage =
                whoScoredClient.fetchPlayerStatsPage(1, pageSize);

        List<WhoScoredPlayerStat> stats =
                new ArrayList<>(firstPage.stats());

        int totalPages = firstPage.paging() != null
                && firstPage.paging().totalPages() != null
                ? firstPage.paging().totalPages()
                : 0;

        if (totalPages <= 0) {
            throw new ProviderUnavailableException(
                    "WHOSCORED_PAGINACION_INVALIDA",
                    "No se pudo completar la actualizacion. WhoScored no informo correctamente la cantidad de paginas."
            );
        }
        //COMENTO ESTO PARA PROBAR CON ALGO MAS CHICO
        for (int page = 2; page <= totalPages; page++) {
        //for (int page = 2; page <= 3; page++) {
            WhoScoredResponse response =
                    whoScoredClient.fetchPlayerStatsPage(page, pageSize);

            stats.addAll(response.stats());
        }

        // 2. Si llegamos hasta aca, WhoScored entrego todas las paginas.
        int created = 0;
        int updated = 0;
        int skipped = 0;

        for (WhoScoredPlayerStat stat : stats) {

            if (stat.playerId() == null
                    || stat.name() == null
                    || stat.name().isBlank()
                    || stat.teamName() == null
                    || stat.teamName().isBlank()
                    || stat.tournamentName() == null
                    || stat.tournamentName().isBlank()) {
                skipped++;
                continue;
            }

            var existing = jugadorRepository.findByWhoscoredId(stat.playerId());

            if (existing.isPresent()) {
                whoScoredPlayerMapper.updateJugador(
                        existing.get(),
                        stat,
                        updatedAt
                );
                updated++;
            } else {
                jugadorRepository.save(
                        whoScoredPlayerMapper.toJugador(stat, updatedAt)
                );
                created++;
            }
        }

        if (created + updated == 0) {
            throw new ProviderUnavailableException(
                    "WHOSCORED_SIN_DATOS_UTILES",
                    "No se pudo completar la actualizacion. WhoScored no produjo registros utiles."
            );
        }

        return RefreshCatalogResponse.success(
                created,
                updated,
                skipped
        );
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
