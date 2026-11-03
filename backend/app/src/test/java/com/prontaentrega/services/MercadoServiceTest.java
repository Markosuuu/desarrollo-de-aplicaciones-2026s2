package com.prontaentrega.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.prontaentrega.models.*;
import com.prontaentrega.models.enums.EstadoAnuncioVenta;
import com.prontaentrega.models.enums.Liga;
import com.prontaentrega.models.enums.Posicion;
import com.prontaentrega.models.enums.TipoOperacion;
import com.prontaentrega.persistence.repository.AnuncioVentaRepository;
import com.prontaentrega.persistence.repository.JugadorRepository;
import com.prontaentrega.persistence.repository.OperacionRepository;
import com.prontaentrega.persistence.repository.InventarioUsuarioRepository;
import com.prontaentrega.persistence.repository.UsuarioRepository;
import com.prontaentrega.services.exceptions.*;
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

/**
 * TODO: Mejorar set de test
 */

@SpringBootTest
@Tag("integracion")
class MercadoServiceTest extends AbstractIntegrationTest {
    private static final AtomicLong EMAIL_SEQUENCE = new AtomicLong();

    @Autowired
    private MercadoService mercadoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JugadorRepository jugadorRepository;

    @Autowired
    private AnuncioVentaRepository anuncioVentaRepository;

    @Autowired
    private InventarioUsuarioRepository inventarioUsuarioRepository;

    @Autowired
    private OperacionRepository operacionRepository;

    @BeforeEach
    void cleanDatabase() {
        operacionRepository.deleteAll();
        anuncioVentaRepository.deleteAll();
        inventarioUsuarioRepository.deleteAll();
        usuarioRepository.deleteAll();
        jugadorRepository.deleteAll();
    }

    @Test
    void sellShouldRejectWhenQuantityExceedsAvailable() {
        Usuario seller = saveUser("Vendedor", uniqueEmail("vendedor@test.com"));
        BolsaDeJugador bolsa = saveJugador("Jugador 1");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();
        inventarioUsuarioRepository.save(new InventarioUsuario(seller, bolsa, 3, 0));

        assertThrows(CantidadDeTokensException.class,
                () -> mercadoService.vender(seller.getId(), jugadorId, 5));
    }

    @Test
    void sellShouldRejectZeroOrNegativeQuantity() {
        Usuario seller = saveUser("Vendedor", uniqueEmail("vendedor2@test.com"));
        BolsaDeJugador bolsa = saveJugador("Jugador 2");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();
        inventarioUsuarioRepository.save(new InventarioUsuario(seller, bolsa, 4, 0));

        assertThrows(CantidadDeTokensException.class,
                () -> mercadoService.vender(seller.getId(), jugadorId, 0));
    }

    @Test
    void buyShouldRejectSelfPurchaseAndInsufficientCredits() {
        Usuario seller = saveUser("Vendedor", uniqueEmail("vendedor3@test.com"));
        BolsaDeJugador bolsa = saveJugador("Jugador 3");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();
        inventarioUsuarioRepository.save(new InventarioUsuario(seller, bolsa, 5, 0));

        UUID anuncioId = mercadoService.vender(seller.getId(), jugadorId, 2).getId();

        assertThrows(VendedorComprandoTokensPropiosException.class,
                () -> mercadoService.comprar(seller.getId(), anuncioId));

        Usuario buyer = saveUser("Comprador", uniqueEmail("comprador@test.com"));
        buyer.setCreditosDisponibles(BigDecimal.ZERO);
        usuarioRepository.save(buyer);

        assertThrows(CreditosInsuficientesException.class,
                () -> mercadoService.comprar(buyer.getId(), anuncioId));
    }

    @Test
    void firstSuccessfulPurchaseShouldWin() {
        Usuario seller = saveUser("Vendedor", uniqueEmail("vendedor4@test.com"));
        Usuario buyer1 = saveUser("Comprador 1", uniqueEmail("buyer1@test.com"));
        Usuario buyer2 = saveUser("Comprador 2", uniqueEmail("buyer2@test.com"));
        BolsaDeJugador bolsa = saveJugador("Jugador 4");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();

        inventarioUsuarioRepository.save(new InventarioUsuario(seller, bolsa, 10, 0));
        buyer1.setCreditosDisponibles(BigDecimal.valueOf(100));
        buyer2.setCreditosDisponibles(BigDecimal.valueOf(100));
        usuarioRepository.save(buyer1);
        usuarioRepository.save(buyer2);

        UUID anuncioId = mercadoService.vender(seller.getId(), jugadorId, 5).getId();
        mercadoService.comprar(buyer1.getId(), anuncioId);

        assertThrows(AnuncioNoPendienteException.class,
                () -> mercadoService.comprar(buyer2.getId(), anuncioId));
    }

    @Test
    void partialPurchaseShouldTransferOnlyRequestedTokensAndKeepListingOpen() {
        Usuario seller = saveUser("Vendedor parcial", uniqueEmail("partial-seller@test.com"));
        Usuario buyer = saveUser("Comprador parcial", uniqueEmail("partial-buyer@test.com"));
        BolsaDeJugador bolsa = saveJugador("Jugador parcial");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();
        inventarioUsuarioRepository.save(new InventarioUsuario(seller, bolsa, 10, 0));

        UUID anuncioId = mercadoService.vender(seller.getId(), jugadorId, 5).getId();
        mercadoService.comprar(buyer.getId(), anuncioId, 2);

        AnuncioVenta anuncioActualizado = anuncioVentaRepository.findById(anuncioId).orElseThrow();
        Usuario compradorActualizado = usuarioRepository.findById(buyer.getId()).orElseThrow();
        Usuario vendedorActualizado = usuarioRepository.findById(seller.getId()).orElseThrow();
        InventarioUsuario tokensComprador = inventarioUsuarioRepository
                .findByUsuarioIdAndJugadorId(buyer.getId(), jugadorId).orElseThrow();
        InventarioUsuario tokensVendedor = inventarioUsuarioRepository
                .findByUsuarioIdAndJugadorId(seller.getId(), jugadorId).orElseThrow();

        assertEquals(EstadoAnuncioVenta.PENDIENTE, anuncioActualizado.getEstado());
        assertEquals(3, anuncioActualizado.getCantidadTokens());
        assertEquals(0, anuncioActualizado.getPrecioTotal().compareTo(BigDecimal.valueOf(15)));
        assertEquals(0, compradorActualizado.getCreditosDisponibles().compareTo(BigDecimal.valueOf(90)));
        assertEquals(0, vendedorActualizado.getCreditosDisponibles().compareTo(BigDecimal.valueOf(110)));
        assertEquals(2, tokensComprador.getTokensDisponibles());
        assertEquals(3, tokensVendedor.getTokensEnVenta());

        List<Operacion> historialComprador = mercadoService.getTransaccionesById(buyer.getId());
        assertEquals(1, historialComprador.size());
        assertEquals(2, historialComprador.get(0).getCantidad());
        assertEquals(0, historialComprador.get(0).getTotal().compareTo(BigDecimal.valueOf(10)));
        assertEquals(buyer.getId(), historialComprador.get(0).getUsuario().getId());
        assertEquals(seller.getId(), historialComprador.get(0).getContraparte().getId());
        assertEquals(jugadorId, historialComprador.get(0).getJugador().getId());
    }

    @Test
    void partialPurchaseShouldRejectQuantityOutsideListingRange() {
        Usuario seller = saveUser("Vendedor límite", uniqueEmail("partial-limit-seller@test.com"));
        Usuario buyer = saveUser("Comprador límite", uniqueEmail("partial-limit-buyer@test.com"));
        BolsaDeJugador bolsa = saveJugador("Jugador límite");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();
        inventarioUsuarioRepository.save(new InventarioUsuario(seller, bolsa, 10, 0));
        UUID anuncioId = mercadoService.vender(seller.getId(), jugadorId, 3).getId();

        assertThrows(CantidadDeTokensException.class,
                () -> mercadoService.comprar(buyer.getId(), anuncioId, 0));
        assertThrows(CantidadDeTokensException.class,
                () -> mercadoService.comprar(buyer.getId(), anuncioId, 4));
    }

    @Test
    void userHistoryShouldReturnCompletedOperations() {
        Usuario seller = saveUser("Vendedor", uniqueEmail("vendedor5@test.com"));
        Usuario buyer = saveUser("Comprador", uniqueEmail("comprador5@test.com"));
        buyer.setCreditosDisponibles(BigDecimal.valueOf(100));
        usuarioRepository.save(buyer);

        BolsaDeJugador bolsa = saveJugador("Jugador 5");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();
        inventarioUsuarioRepository.save(new InventarioUsuario(seller, bolsa, 10, 0));
        UUID anuncioId = mercadoService.vender(seller.getId(), jugadorId, 3).getId();
        mercadoService.comprar(buyer.getId(), anuncioId);

        List<Operacion> history = mercadoService.getTransaccionesById(seller.getId());
        assertEquals(1, history.size());
        assertEquals(TipoOperacion.VENTA, history.get(0).getTipo());
    }

    private Usuario saveUser(String nombre, String correo) {
        Usuario usuario = new Usuario(nombre, correo, "password123");
        return usuarioRepository.save(usuario);
    }

    private BolsaDeJugador saveJugador(String nombre) {
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
        return persistida;
    }

    private String uniqueEmail(String base) {
        long suffix = EMAIL_SEQUENCE.getAndIncrement();
        return base.replace("@", "+" + suffix + "@");
    }
}
