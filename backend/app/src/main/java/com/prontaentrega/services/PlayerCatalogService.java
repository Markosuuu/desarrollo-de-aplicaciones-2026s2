package com.prontaentrega.services;

import com.prontaentrega.controllers.dtos.PlayerResponse;
import com.prontaentrega.models.CatalogSnapshot;
import com.prontaentrega.models.Jugador;
import com.prontaentrega.repository.CatalogSnapshotRepository;
import com.prontaentrega.repository.JugadorRepository;
import com.prontaentrega.services.dto.CatalogResponse;
import com.prontaentrega.services.exceptions.CatalogUnavailableException;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PlayerCatalogService {
    private final JugadorRepository jugadorRepository;
    private final CatalogSnapshotRepository catalogSnapshotRepository;

    public PlayerCatalogService(JugadorRepository jugadorRepository,
                               CatalogSnapshotRepository catalogSnapshotRepository) {
        this.jugadorRepository = jugadorRepository;
        this.catalogSnapshotRepository = catalogSnapshotRepository;
    }

    @PostConstruct
    public void seedCatalogIfEmpty() {
        if (jugadorRepository.count() > 0) {
            return;
        }

        catalogSnapshotRepository.save(new CatalogSnapshot(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                CatalogSnapshot.EstadoCatalogo.VALIDO,
                null
        ));

        List<Jugador> jugadores = List.of(
                new Jugador("Kylian Mbappe", "Real Madrid", "La Liga", new BigDecimal("1.0"), LocalDateTime.now(), "fixture", 10),
                new Jugador("Vinicius Junior", "Real Madrid", "La Liga", new BigDecimal("1.0"), LocalDateTime.now(), "fixture", 8),
                new Jugador("Erling Haaland", "Manchester City", "Premier League", new BigDecimal("1.0"), LocalDateTime.now(), "fixture", 9),
                new Jugador("Jude Bellingham", "Real Madrid", "La Liga", new BigDecimal("1.0"), LocalDateTime.now(), "fixture", 7),
                new Jugador("Marcus Rashford", "Manchester United", "Premier League", new BigDecimal("1.0"), LocalDateTime.now(), "fixture", 12)
        );

        jugadorRepository.saveAll(jugadores);
    }

    public CatalogResponse search(String liga, String equipo, String nombre, int page, int perPage) {
        int safePage = Math.max(page, 1);
        int safePerPage = Math.max(Math.min(perPage, 50), 1);

        Pageable pageable = PageRequest.of(safePage - 1, safePerPage);
        Page<Jugador> result = jugadorRepository.search(normalize(liga), normalize(equipo), normalize(nombre), pageable);

        if (catalogSnapshotRepository.findTopByEstadoOrderByIniciadoEnDesc(CatalogSnapshot.EstadoCatalogo.VALIDO).isEmpty() && result.isEmpty()) {
            throw new CatalogUnavailableException("CATALOGO_NO_DISPONIBLE");
        }

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

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
