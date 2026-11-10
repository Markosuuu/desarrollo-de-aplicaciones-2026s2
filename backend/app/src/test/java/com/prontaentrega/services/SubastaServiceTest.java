package com.prontaentrega.services;

import com.prontaentrega.models.BolsaDeJugador;
import com.prontaentrega.models.EstadisticasDeportivas;
import com.prontaentrega.models.InventarioUsuario;
import com.prontaentrega.models.Jugador;
import com.prontaentrega.models.Oferta;
import com.prontaentrega.models.Usuario;
import com.prontaentrega.models.Subasta;
import com.prontaentrega.persistence.repository.InventarioUsuarioRepository;
import com.prontaentrega.persistence.repository.JugadorRepository;
import com.prontaentrega.persistence.repository.OfertaRepository;
import com.prontaentrega.persistence.repository.OperacionRepository;
import com.prontaentrega.persistence.repository.SubastaRepository;
import com.prontaentrega.persistence.repository.UsuarioRepository;
import com.prontaentrega.models.exception.CantidadDeTokensException;
import com.prontaentrega.services.exceptions.OfertaInvalidaException;
import com.prontaentrega.services.exceptions.IdempotencyKeyException;
import com.prontaentrega.utils.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Tag("integracion")
class SubastaServiceTest extends AbstractIntegrationTest {

    @Autowired
    private SubastaService subastaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JugadorRepository jugadorRepository;

    @Autowired
    private InventarioUsuarioRepository inventarioUsuarioRepository;

    @Autowired
    private OfertaRepository ofertaRepository;

    @Autowired
    private SubastaRepository subastaRepository;

    @Autowired
    private OperacionRepository operacionRepository;

    @BeforeEach
    void cleanDatabase() {
        operacionRepository.deleteAll();
        ofertaRepository.deleteAll();
        subastaRepository.deleteAll();
        inventarioUsuarioRepository.deleteAll();
        usuarioRepository.deleteAll();
        jugadorRepository.deleteAll();
    }

    @AfterEach
    void cleanDatabaseAfterTest() {
        cleanDatabase();
    }

    @Test
    void shouldCreateAuctionWithValidData() {
        Usuario seller = saveUser("Vendedor", "seller@test.com");
        BolsaDeJugador bolsa = saveJugador("Jugador 1");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();
        inventarioUsuarioRepository.save(new InventarioUsuario(seller, bolsa, 10, 0));

        Subasta subasta = subastaService.crearSubasta(
                seller.getId(),
                jugadorId,
                3,
                BigDecimal.valueOf(150),
                futureDeadline());

        assertTrue(subasta.getActiva());
        assertEquals(3, subasta.getCantidadOfrecida());
        assertNull(subasta.getOfertaActual());
    }

    @Test
    void shouldRejectInvalidAuctionQuantity() {
        Usuario seller = saveUser("Vendedor", "seller2@test.com");
        BolsaDeJugador bolsa = saveJugador("Jugador 2");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();
        inventarioUsuarioRepository.save(new InventarioUsuario(seller, bolsa, 10, 0));

        assertThrows(CantidadDeTokensException.class,
                () -> subastaService.crearSubasta(seller.getId(), jugadorId, 0, BigDecimal.valueOf(15), futureDeadline()));
    }

    @Test
    void shouldRejectBidBelowCurrentPriceAndOwnerBid() {
        Usuario seller = saveUser("Vendedor", "seller3@test.com");
        Usuario buyer = saveUser("Comprador", "buyer3@test.com");
        Usuario secondBuyer = saveUser("Segundo comprador", "buyer4@test.com");
        BolsaDeJugador bolsa = saveJugador("Jugador 3");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();
        inventarioUsuarioRepository.save(new InventarioUsuario(seller, bolsa, 10, 0));

        Subasta subasta = subastaService.crearSubasta(seller.getId(), jugadorId, 2, BigDecimal.valueOf(100), futureDeadline());

        Oferta primeraOferta = subastaService.ofertar(buyer.getId(), subasta.getId(), BigDecimal.valueOf(150), "bid-1");
        Oferta reintento = subastaService.ofertar(buyer.getId(), subasta.getId(), BigDecimal.valueOf(150), "bid-1");
        assertEquals(primeraOferta.getId(), reintento.getId());
        assertEquals(1, ofertaRepository.findBySubastaId(subasta.getId()).size());
        assertThrows(
                IdempotencyKeyException.class,
                () -> subastaService.ofertar(
                        buyer.getId(), subasta.getId(), BigDecimal.valueOf(160), "bid-1"));

        Oferta nuevaOferta = subastaService.ofertar(
                buyer.getId(), subasta.getId(), BigDecimal.valueOf(160), "bid-2");
        assertNotEquals(primeraOferta.getId(), nuevaOferta.getId());
        assertEquals(2, ofertaRepository.findBySubastaId(subasta.getId()).size());
        assertThrows(OfertaInvalidaException.class,
            () -> subastaService.ofertar(secondBuyer.getId(), subasta.getId(), BigDecimal.valueOf(120), "bid-2"));

        assertThrows(OfertaInvalidaException.class,
            () -> subastaService.ofertar(seller.getId(), subasta.getId(), BigDecimal.valueOf(200), "bid-3"));
    }

        @Test
        void shouldSettleAuctionAndTransferTokensToHighestBidder() {
        Usuario seller = saveUser("Vendedor cierre", "seller-close@test.com");
        Usuario buyer = saveUser("Ganador", "buyer-close@test.com");
        BolsaDeJugador bolsa = saveJugador("Jugador cierre");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();
        inventarioUsuarioRepository.save(new InventarioUsuario(seller, bolsa, 5, 0));

        Subasta subasta = subastaService.crearSubasta(
            seller.getId(), jugadorId, 2, BigDecimal.valueOf(50), futureDeadline());
        subastaService.ofertar(buyer.getId(), subasta.getId(), BigDecimal.valueOf(80), "winner-bid");
        subastaService.cerrarSubasta(seller.getId(), subasta.getId(), "Por propietario");

        Usuario savedSeller = usuarioRepository.findById(seller.getId()).orElseThrow();
        Usuario savedBuyer = usuarioRepository.findById(buyer.getId()).orElseThrow();
        InventarioUsuario sellerInventory = inventarioUsuarioRepository
            .findByUsuarioIdAndJugadorId(seller.getId(), jugadorId).orElseThrow();
        InventarioUsuario buyerInventory = inventarioUsuarioRepository
            .findByUsuarioIdAndJugadorId(buyer.getId(), jugadorId).orElseThrow();

        assertEquals(0, savedSeller.getCreditosDisponibles().compareTo(BigDecimal.valueOf(1080)));
        assertEquals(0, savedBuyer.getCreditosDisponibles().compareTo(BigDecimal.valueOf(920)));
        assertEquals(3, sellerInventory.getTokensDisponibles());
        assertEquals(0, sellerInventory.getTokensEnVenta());
        assertEquals(2, buyerInventory.getTokensDisponibles());
            assertEquals(2,
                    operacionRepository.findByUsuarioId(seller.getId(), Pageable.unpaged()).getTotalElements()
                            + operacionRepository.findByUsuarioId(buyer.getId(), Pageable.unpaged()).getTotalElements());
        }

    @Test
    void shouldFallBackToNextBidderWhenHighestBidderCannotPay() {
        Usuario seller = saveUser("Vendedor fallback", "seller-fallback@test.com");
        Usuario highestBidder = saveUser("Mayor postor", "highest-fallback@test.com");
        Usuario nextBidder = saveUser("Siguiente postor", "next-fallback@test.com");
        BolsaDeJugador bolsa = saveJugador("Jugador fallback");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();
        inventarioUsuarioRepository.save(new InventarioUsuario(seller, bolsa, 3, 0));

        Subasta subasta = subastaService.crearSubasta(
            seller.getId(), jugadorId, 1, BigDecimal.valueOf(10), futureDeadline());
        subastaService.ofertar(nextBidder.getId(), subasta.getId(), BigDecimal.valueOf(70), "next");
        subastaService.ofertar(highestBidder.getId(), subasta.getId(), BigDecimal.valueOf(90), "highest");
        highestBidder.setCreditosDisponibles(BigDecimal.ZERO);
        usuarioRepository.save(highestBidder);

        subastaService.cerrarSubasta(seller.getId(), subasta.getId(), "Por propietario");

        Subasta cerrada = subastaService.obtenerSubasta(subasta.getId());
        assertEquals(nextBidder.getId(), cerrada.getOfertaActual().getUsuarioId());
        assertEquals(0, usuarioRepository.findById(seller.getId()).orElseThrow()
            .getCreditosDisponibles().compareTo(BigDecimal.valueOf(1070)));
    }

    @Test
    void shouldReturnReservedTokensWhenAuctionHasNoBids() throws InterruptedException {
        Usuario seller = saveUser("Vendedor retorno", "seller-return@test.com");
        BolsaDeJugador bolsa = saveJugador("Jugador retorno");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();
        InventarioUsuario inventario = new InventarioUsuario(seller, bolsa, 4, 0);
        inventario.ponerTokensEnVenta(3);
        inventarioUsuarioRepository.save(inventario);

        Subasta subasta = new Subasta(
            seller, bolsa, 3, BigDecimal.valueOf(10), LocalDateTime.now().minusMinutes(1));
        subastaRepository.save(subasta);
        subastaService.resolverSubastasVencidas();

        InventarioUsuario inventory = inventarioUsuarioRepository
            .findByUsuarioIdAndJugadorId(seller.getId(), jugadorId).orElseThrow();
        assertEquals(4, inventory.getTokensDisponibles());
        assertEquals(0, inventory.getTokensEnVenta());
        assertTrue(operacionRepository.findByUsuarioId(seller.getId(), Pageable.unpaged()).isEmpty());
    }

    @Test
    void shouldListActiveAuctionsWithPagination() {
        Usuario seller = saveUser("Vendedor", "seller@test.com");
        BolsaDeJugador bolsa = saveJugador("Jugador 1");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();

        inventarioUsuarioRepository.save(new InventarioUsuario(seller, bolsa, 10, 0));

        subastaService.crearSubasta(
                seller.getId(),
                jugadorId,
                3,
                BigDecimal.valueOf(150),
                futureDeadline()
        );

        Page<Subasta> result = subastaService.listarSubastas(1, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().get(0).getActiva());
    }

    @Test
    void shouldNormalizePageAndSizeWhenListingAuctions() {
        Usuario seller = saveUser("Vendedor", "seller@test.com");
        BolsaDeJugador bolsa = saveJugador("Jugador 1");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();

        inventarioUsuarioRepository.save(new InventarioUsuario(seller, bolsa, 10, 0));

        subastaService.crearSubasta(
                seller.getId(),
                jugadorId,
                3,
                BigDecimal.valueOf(150),
                futureDeadline()
        );

        Page<Subasta> result = subastaService.listarSubastas(0, 0);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getSize());
        assertEquals(0, result.getNumber());
    }

    @Test
    void shouldListAuctionsFilteredByUserAndPlayerName() {
        Usuario seller = saveUser("Vendedor", "seller@test.com");
        BolsaDeJugador bolsa = saveJugador("Jugador 1");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();

        inventarioUsuarioRepository.save(new InventarioUsuario(seller, bolsa, 10, 0));

        subastaService.crearSubasta(
                seller.getId(),
                jugadorId,
                3,
                BigDecimal.valueOf(150),
                futureDeadline()
        );

        Page<Subasta> result = subastaService.listarSubastasPorNombre(
                1, 10, "Vendedor", "Jugador 1"
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
    }

    @Test
    void shouldNormalizePageAndSizeWhenListingAuctionsByName() {
        Usuario seller = saveUser("Vendedor", "seller@test.com");
        BolsaDeJugador bolsa = saveJugador("Jugador 1");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();

        inventarioUsuarioRepository.save(new InventarioUsuario(seller, bolsa, 10, 0));

        subastaService.crearSubasta(
                seller.getId(),
                jugadorId,
                3,
                BigDecimal.valueOf(150),
                futureDeadline()
        );

        Page<Subasta> result = subastaService.listarSubastasPorNombre(
                0, 0, "Vendedor", "Jugador 1"
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getSize());
        assertEquals(0, result.getNumber());
    }

    private Usuario saveUser(String nombre, String correo) {
        Usuario usuario = new Usuario(nombre, correo, "hash");
        usuario.setCreditosDisponibles(BigDecimal.valueOf(1000));
        return usuarioRepository.save(usuario);
    }

    private LocalDateTime futureDeadline() {
        return LocalDateTime.now().plusDays(1).withSecond(0).withNano(0);
    }

    private BolsaDeJugador saveJugador(String nombre) {
        Jugador jugador = new Jugador(
                (int) (System.currentTimeMillis() % 100000),
                nombre,
                "Equipo X",
                com.prontaentrega.models.enums.Liga.LALIGA,
                com.prontaentrega.models.enums.Posicion.FORWARD,
                25,
                180,
                70);
        BolsaDeJugador bolsa = new BolsaDeJugador(
                new EstadisticasDeportivas(jugador, true, 10, 5, BigDecimal.valueOf(2.5), BigDecimal.valueOf(1.5),
                        BigDecimal.valueOf(1.2), BigDecimal.valueOf(0.7), BigDecimal.valueOf(7.5), 900),
                BigDecimal.valueOf(5),
                LocalDateTime.now(),
                "WhoScored",
                100);

        return jugadorRepository.save(bolsa);
    }
}
