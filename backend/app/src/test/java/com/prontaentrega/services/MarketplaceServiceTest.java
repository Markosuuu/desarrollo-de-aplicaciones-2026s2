package com.prontaentrega.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.prontaentrega.models.AnuncioVenta;
import com.prontaentrega.models.BolsaDeJugador;
import com.prontaentrega.models.EstadisticasDeportivas;
import com.prontaentrega.models.Jugador;
import com.prontaentrega.models.Liga;
import com.prontaentrega.models.Posicion;
import com.prontaentrega.models.Usuario;
import com.prontaentrega.models.UsuarioJugadorToken;
import com.prontaentrega.persistence.repository.AnuncioVentaRepository;
import com.prontaentrega.persistence.repository.JugadorRepository;
import com.prontaentrega.persistence.repository.OperacionRepository;
import com.prontaentrega.persistence.repository.UsuarioJugadorTokenRepository;
import com.prontaentrega.persistence.repository.UsuarioRepository;
import com.prontaentrega.services.dto.OperacionResponse;
import com.prontaentrega.services.exceptions.ValidationException;
import com.prontaentrega.utils.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Tag("integracion")
class MarketplaceServiceTest extends AbstractIntegrationTest {
    private static final AtomicLong EMAIL_SEQUENCE = new AtomicLong();

    @Autowired
    private MarketplaceService marketplaceService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JugadorRepository jugadorRepository;

    @Autowired
    private AnuncioVentaRepository anuncioVentaRepository;

    @Autowired
    private UsuarioJugadorTokenRepository usuarioJugadorTokenRepository;

    @Autowired
    private OperacionRepository operacionRepository;

    @BeforeEach
    void cleanDatabase() {
        operacionRepository.deleteAll();
        anuncioVentaRepository.deleteAll();
        usuarioJugadorTokenRepository.deleteAll();
        usuarioRepository.deleteAll();
        jugadorRepository.deleteAll();
    }

    @Test
    void sellShouldRejectWhenQuantityExceedsAvailable() {
        Usuario seller = saveUser("Vendedor", uniqueEmail("vendedor@test.com"));
        UUID jugadorId = saveJugador("Jugador 1");
        usuarioJugadorTokenRepository.save(new UsuarioJugadorToken(seller.getId(), jugadorId, 3, 0));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> marketplaceService.sellTokens(seller.getId(), jugadorId, 5));

        assertEquals("TOKENS_INSUFICIENTES", ex.getCode());
    }

    @Test
    void sellShouldRejectZeroOrNegativeQuantity() {
        Usuario seller = saveUser("Vendedor", uniqueEmail("vendedor2@test.com"));
        UUID jugadorId = saveJugador("Jugador 2");
        usuarioJugadorTokenRepository.save(new UsuarioJugadorToken(seller.getId(), jugadorId, 4, 0));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> marketplaceService.sellTokens(seller.getId(), jugadorId, 0));

        assertEquals("CANTIDAD_INVALIDA", ex.getCode());
    }

    @Test
    void buyShouldRejectSelfPurchaseAndInsufficientCredits() {
        Usuario seller = saveUser("Vendedor", uniqueEmail("vendedor3@test.com"));
        UUID jugadorId = saveJugador("Jugador 3");
        usuarioJugadorTokenRepository.save(new UsuarioJugadorToken(seller.getId(), jugadorId, 5, 0));

        UUID anuncioId = marketplaceService.sellTokens(seller.getId(), jugadorId, 2).id();

        ValidationException selfBuy = assertThrows(ValidationException.class,
                () -> marketplaceService.buyTokens(seller.getId(), anuncioId));
        assertEquals("COMPRA_PROPIA", selfBuy.getCode());

        Usuario buyer = saveUser("Comprador", uniqueEmail("comprador@test.com"));
        buyer.setCreditosDisponibles(BigDecimal.ZERO);
        usuarioRepository.save(buyer);

        ValidationException creditError = assertThrows(ValidationException.class,
                () -> marketplaceService.buyTokens(buyer.getId(), anuncioId));
        assertEquals("CREDITOS_INSUFICIENTES", creditError.getCode());
    }

    @Test
    void firstSuccessfulPurchaseShouldWin() {
        Usuario seller = saveUser("Vendedor", uniqueEmail("vendedor4@test.com"));
        Usuario buyer1 = saveUser("Comprador 1", uniqueEmail("buyer1@test.com"));
        Usuario buyer2 = saveUser("Comprador 2", uniqueEmail("buyer2@test.com"));
        UUID jugadorId = saveJugador("Jugador 4");

        usuarioJugadorTokenRepository.save(new UsuarioJugadorToken(seller.getId(), jugadorId, 10, 0));
        buyer1.setCreditosDisponibles(BigDecimal.valueOf(100));
        buyer2.setCreditosDisponibles(BigDecimal.valueOf(100));
        usuarioRepository.save(buyer1);
        usuarioRepository.save(buyer2);

        UUID anuncioId = marketplaceService.sellTokens(seller.getId(), jugadorId, 5).id();
        marketplaceService.buyTokens(buyer1.getId(), anuncioId);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> marketplaceService.buyTokens(buyer2.getId(), anuncioId));
        assertEquals("ANUNCIO_NO_DISPONIBLE", ex.getCode());
    }

    @Test
    void userHistoryShouldReturnCompletedOperations() {
        Usuario seller = saveUser("Vendedor", uniqueEmail("vendedor5@test.com"));
        Usuario buyer = saveUser("Comprador", uniqueEmail("comprador5@test.com"));
        buyer.setCreditosDisponibles(BigDecimal.valueOf(100));
        usuarioRepository.save(buyer);

        UUID jugadorId = saveJugador("Jugador 5");
        usuarioJugadorTokenRepository.save(new UsuarioJugadorToken(seller.getId(), jugadorId, 10, 0));
        UUID anuncioId = marketplaceService.sellTokens(seller.getId(), jugadorId, 3).id();
        marketplaceService.buyTokens(buyer.getId(), anuncioId);

        List<OperacionResponse> history = marketplaceService.getUserTransactions(seller.getId());
        assertEquals(1, history.size());
        assertEquals("VENTA", history.get(0).tipo());
    }

    private Usuario saveUser(String nombre, String correo) {
        Usuario usuario = new Usuario(nombre, correo, "password123");
        return usuarioRepository.save(usuario);
    }

    private UUID saveJugador(String nombre) {
        Jugador jugador = new Jugador((int) (System.currentTimeMillis() % 100000),
                nombre,
                "Equipo X",
                Liga.LALIGA,
                Posicion.FORWARD,
                25,
                180,
                70);
        BolsaDeJugador bolsa = new BolsaDeJugador(
                new EstadisticasDeportivas(jugador, true, 10, 5, BigDecimal.valueOf(2.5), BigDecimal.valueOf(1.5),
                        BigDecimal.valueOf(1.2), BigDecimal.valueOf(0.7), BigDecimal.valueOf(7.5), 900),
                BigDecimal.valueOf(5),
                LocalDateTime.now(),
                "WhoScored",
                100
        );
        BolsaDeJugador persistida = jugadorRepository.save(bolsa);
        return persistida.getEstadisticasDeportivas().getJugador().getId();
    }

    private String uniqueEmail(String base) {
        long suffix = EMAIL_SEQUENCE.getAndIncrement();
        return base.replace("@", "+" + suffix + "@");
    }
}
