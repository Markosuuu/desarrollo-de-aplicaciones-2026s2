package com.prontaentrega.services;

import com.prontaentrega.models.BolsaDeJugador;
import com.prontaentrega.models.Liga;
import com.prontaentrega.persistence.repository.JugadorRepository;
import com.prontaentrega.services.dto.RefreshCatalogResponse;
import com.prontaentrega.services.dto.external.PlayerStat;
import com.prontaentrega.services.dto.external.PlayerScrapperResponse;
import com.prontaentrega.services.exceptions.ProviderUnavailableException;
import com.prontaentrega.services.scraping.PlayerScrapper;
import com.prontaentrega.services.scraping.whoscoredscrapper.ScrappedPlayerMapper;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de catalogo de jugadores.
 * Orquesta lectura local, obtencion de WhoScored y persistencia segura sin
 * mantener las reglas de dominio en sus componentes correspondientes.
 */
@Service
public class PlayerCatalogService {
    @PersistenceContext
    private EntityManager entityManager;

    private final JugadorRepository jugadorRepository;
    private final PlayerScrapper playerScrapper;
    private final ScrappedPlayerMapper scrappedPlayerMapper;

    public PlayerCatalogService(JugadorRepository jugadorRepository,
                                PlayerScrapper playerScrapper,
                                ScrappedPlayerMapper scrappedPlayerMapper) {
        this.jugadorRepository = jugadorRepository;
        this.playerScrapper = playerScrapper;
        this.scrappedPlayerMapper = scrappedPlayerMapper;
    }

    /**
     * Busca jugadores en el catalogo local.
     *
     * @param liga Liga a la que pertenece el jugador
     * @param equipo Equipo al que pertenece el jugador
     * @param nombre Nombre del jugador
     * @param page Número de página
     * @param perPage Cantidad de resultados por página
     * @return Page<BolsaDeJugador> Página de jugadores encontrados con sus estadísticas y cotización
     */
    @Transactional(readOnly = true)
    public Page<BolsaDeJugador> search(Liga liga, String equipo, String nombre, int page, int perPage) {
        int safePage = Math.max(page, 1);
        int safePerPage = Math.clamp(perPage, 1, 50);

        Pageable pageable = PageRequest.of(safePage - 1, safePerPage);
        return jugadorRepository.search(liga, normalize(equipo), normalize(nombre), pageable);
    }

    /**
     * Actualiza el catalogo desde WhoScored preservando los datos locales
     * ante fallos del proveedor.
     */
    @Transactional
    public RefreshCatalogResponse refreshFromWhoScored() {
        LocalDateTime updatedAt = LocalDateTime.now(ZoneId.systemDefault());
        final int pageSize = 20;

        // 1. Descargar la primera página para obtener la paginación.
        PlayerScrapperResponse firstPage =
                playerScrapper.fetchPlayerStatsPage(1, pageSize);

        int totalPages = firstPage.paging() != null
                && firstPage.paging().totalPages() != null
                ? firstPage.paging().totalPages()
                : 0;

        if (totalPages <= 0) {
            throw new ProviderUnavailableException(
                    "PAGINACION_INVALIDA",
                    "No se pudo completar la actualizacion. El proveedor no informo correctamente la cantidad de paginas."
            );
        }

        int created = 0;
        int updated = 0;
        int skipped = 0;

        for (int page = 1; page <= totalPages; page++) {
            PlayerScrapperResponse response = page == 1
                    ? firstPage
                    : playerScrapper.fetchPlayerStatsPage(page, pageSize);

            int[] pageResults = processPage(response, updatedAt);

            created += pageResults[0];
            updated += pageResults[1];
            skipped += pageResults[2];

            entityManager.flush();
            entityManager.clear();
        }

        if (created + updated == 0) {
            throw new ProviderUnavailableException(
                    "SIN_DATOS_UTILES",
                    "No se pudo completar la actualizacion. El proveedor no produjo registros utiles."
            );
        }

        return RefreshCatalogResponse.success(
                created,
                updated,
                skipped,
                playerScrapper.source()
        );
    }

    /**
     * Procesa los jugadores de una página y contabiliza los resultados.
     *
     * @return arreglo con la cantidad de jugadores creados, actualizados y omitidos.
     */
    private int[] processPage(
            PlayerScrapperResponse response,
            LocalDateTime updatedAt
    ) {
        int created = 0;
        int updated = 0;
        int skipped = 0;

        for (PlayerStat stat : response.stats()) {

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

            Optional<BolsaDeJugador> existing =
                    jugadorRepository.findByWhoscoredId(stat.playerId());

            if (existing.isPresent()) {
                BolsaDeJugador bolsa = existing.get();

                scrappedPlayerMapper.updateBolsaDeJugador(
                        bolsa,
                        stat,
                        updatedAt
                );

                jugadorRepository.save(bolsa);
                updated++;
            } else {
                jugadorRepository.save(
                        scrappedPlayerMapper.toBolsaDeJugador(
                                stat,
                                updatedAt
                        )
                );
                created++;
            }
        }

        return new int[]{created, updated, skipped};
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
