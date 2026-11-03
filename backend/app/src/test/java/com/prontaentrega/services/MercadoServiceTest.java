package com.prontaentrega.services;

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
import org.springframework.data.domain.Page;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void getAnunciosDevuelveElContenidoPaginadoTest() {
        Usuario seller = saveUser("Vendedor", uniqueEmail("anuncios-seller@test.com"));
        BolsaDeJugador bolsa = saveJugador("Jugador anuncios");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();
        inventarioUsuarioRepository.save(new InventarioUsuario(seller, bolsa, 10, 0));

        for (int i = 0; i < 3; i++) {
            mercadoService.vender(seller.getId(), jugadorId, 1);
        }

        Page<AnuncioVenta> primeraPagina = mercadoService.getAnuncios(1, 2);
        Page<AnuncioVenta> segundaPagina = mercadoService.getAnuncios(2, 2);

        assertEquals(2, primeraPagina.getContent().size());
        assertEquals(3, primeraPagina.getTotalElements());
        assertEquals(2, primeraPagina.getTotalPages());
        assertEquals(1, segundaPagina.getContent().size());
        assertEquals(1, segundaPagina.getNumber());
    }

    @Test
    void getAnunciosDevuelveUnicamentePendientesTest() {
        Usuario seller = saveUser("Vendedor", uniqueEmail("anuncios-seller2@test.com"));
        Usuario buyer = saveUser("Comprador", uniqueEmail("anuncios-buyer2@test.com"));
        buyer.setCreditosDisponibles(BigDecimal.valueOf(100));
        usuarioRepository.save(buyer);
        BolsaDeJugador bolsa = saveJugador("Jugador anuncios 2");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();
        inventarioUsuarioRepository.save(new InventarioUsuario(seller, bolsa, 10, 0));

        UUID vendidoId = mercadoService.vender(seller.getId(), jugadorId, 1).getId();
        UUID pendienteId = mercadoService.vender(seller.getId(), jugadorId, 1).getId();
        mercadoService.comprar(buyer.getId(), vendidoId);

        Page<AnuncioVenta> resultado = mercadoService.getAnuncios(1, 10);

        assertEquals(1, resultado.getTotalElements());
        assertEquals(pendienteId, resultado.getContent().getFirst().getId());
        assertEquals(EstadoAnuncioVenta.PENDIENTE, resultado.getContent().getFirst().getEstado());
        assertNotEquals(EstadoAnuncioVenta.COMPLETADA, resultado.getContent().getFirst().getEstado());
        assertNotEquals(EstadoAnuncioVenta.CANCELADA, resultado.getContent().getFirst().getEstado());
    }

    @Test
    void getAnunciosDevuelveLasPaginasNegativasConUnaPagina() {
        Usuario seller = saveUser("Vendedor", uniqueEmail("anuncios-seller3@test.com"));
        BolsaDeJugador bolsa = saveJugador("Jugador anuncios 3");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();
        inventarioUsuarioRepository.save(new InventarioUsuario(seller, bolsa, 10, 0));
        mercadoService.vender(seller.getId(), jugadorId, 1);
        mercadoService.vender(seller.getId(), jugadorId, 1);

        // page <= 0 se normaliza a la primera página (índice 0) y size <= 0 a 1
        Page<AnuncioVenta> cero = mercadoService.getAnuncios(0, 0);
        Page<AnuncioVenta> negativos = mercadoService.getAnuncios(-5, -3);

        assertEquals(0, cero.getNumber());
        assertEquals(1, cero.getSize());
        assertEquals(1, cero.getContent().size());
        assertEquals(0, negativos.getNumber());
        assertEquals(1, negativos.getSize());
        assertEquals(2, negativos.getTotalElements());
    }

    @Test
    void getAnunciosDevuelveVacioCuandoNoHayAnunciosTest() {
        Page<AnuncioVenta> resultado = mercadoService.getAnuncios(1, 10);

        assertEquals(0, resultado.getTotalElements());
        assertTrue(resultado.getContent().isEmpty());
    }

    @Test
    void comprarPeroElVendedorTieneMenosTokensEnVentaQueElAnuncioTest() {
        Usuario seller = saveUser("Vendedor", uniqueEmail("desync-seller@test.com"));
        Usuario buyer = saveUser("Comprador", uniqueEmail("desync-buyer@test.com"));
        buyer.setCreditosDisponibles(BigDecimal.valueOf(100));
        usuarioRepository.save(buyer);
        BolsaDeJugador bolsa = saveJugador("Jugador desync");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();
        inventarioUsuarioRepository.save(new InventarioUsuario(seller, bolsa, 10, 0));

        UUID anuncioId = mercadoService.vender(seller.getId(), jugadorId, 5).getId();

        // creando la inconsistencia del vendedor
        inventarioUsuarioRepository.deleteAll();
        inventarioUsuarioRepository.save(new InventarioUsuario(seller, bolsa, 10, 0));

        assertThrows(CantidadDeTokensException.class,
                () -> mercadoService.comprar(buyer.getId(), anuncioId, 2));
    }



    @Test
    void cancelarVentaDevuelveLosTokensAlDuenioYElAnuncioPasaASerCancelado() {
        Usuario vendedor = saveUser("Vendedor", uniqueEmail("cancel-seller@test.com"));
        BolsaDeJugador bolsa = saveJugador("Jugador cancel");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();
        inventarioUsuarioRepository.save(new InventarioUsuario(vendedor, bolsa, 10, 0));

        UUID anuncioId = mercadoService.vender(vendedor.getId(), jugadorId, 5).getId();

        // Chequeo sobre el estado de ese anuncio

        AnuncioVenta anuncio = anuncioVentaRepository.findById(anuncioId).orElseThrow();
        InventarioUsuario inventario = inventarioUsuarioRepository
                .findByUsuarioIdAndJugadorId(vendedor.getId(), jugadorId).orElseThrow();
        assertEquals(EstadoAnuncioVenta.PENDIENTE, anuncio.getEstado());
        assertEquals(5, inventario.getTokensDisponibles());
        assertEquals(5, inventario.getTokensEnVenta());

        mercadoService.cancelarVenta(vendedor.getId(), anuncioId);

        AnuncioVenta anunciocancelado = anuncioVentaRepository.findById(anuncioId).orElseThrow();
        InventarioUsuario inventariocancelado = inventarioUsuarioRepository
                .findByUsuarioIdAndJugadorId(vendedor.getId(), jugadorId).orElseThrow();

        assertEquals(EstadoAnuncioVenta.CANCELADA, anunciocancelado.getEstado());
        assertEquals(10, inventariocancelado.getTokensDisponibles());
        assertEquals(0, inventariocancelado.getTokensEnVenta());
    }

    @Test
    void cancelarVentaDevuelveAlVendedorLosTokensDeUnaCompraParcialTest() {
        Usuario vendedor = saveUser("Vendedor", uniqueEmail("cancel-partial-seller@test.com"));
        Usuario comprador = saveUser("Comprador", uniqueEmail("cancel-partial-buyer@test.com"));
        BolsaDeJugador bolsa = saveJugador("Jugador cancel parcial");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();
        inventarioUsuarioRepository.save(new InventarioUsuario(vendedor, bolsa, 10, 0));

        UUID anuncioId = mercadoService.vender(vendedor.getId(), jugadorId, 5).getId();
        mercadoService.comprar(comprador.getId(), anuncioId, 2);

        mercadoService.cancelarVenta(vendedor.getId(), anuncioId);

        InventarioUsuario inventario = inventarioUsuarioRepository
                .findByUsuarioIdAndJugadorId(vendedor.getId(), jugadorId).orElseThrow();

        // 10 iniciales - 2 vendidos = 8, y ninguno queda en venta
        assertEquals(8, inventario.getTokensDisponibles());
        assertEquals(0, inventario.getTokensEnVenta());
    }

    @Test
    void cancelarVentaQuitaElAnuncioDePendientes() {
        Usuario vendedor = saveUser("Vendedor", uniqueEmail("cancel-list-seller@test.com"));
        BolsaDeJugador bolsa = saveJugador("Jugador cancel lista");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();
        inventarioUsuarioRepository.save(new InventarioUsuario(vendedor, bolsa, 10, 0));

        UUID anuncioId = mercadoService.vender(vendedor.getId(), jugadorId, 5).getId();
        mercadoService.cancelarVenta(vendedor.getId(), anuncioId);

        assertEquals(0, mercadoService.getAnuncios(1, 10).getTotalElements());
    }

    @Test
    void cancelarVentaFallaCuandoNoExisteUnAnuncio() {
        Usuario vendedor = saveUser("Vendedor", uniqueEmail("cancel-missing@test.com"));

        assertThrows(AnuncioNoEncontradoException.class,
                () -> mercadoService.cancelarVenta(vendedor.getId(), UUID.randomUUID()));
    }

    @Test
    void cancelarVentaFallaCuandoElQueCancelaNoEsElDuenioDelAnuncio() {
        Usuario seller = saveUser("Vendedor", uniqueEmail("cancel-owner-seller@test.com"));
        Usuario otro = saveUser("Otro usuario", uniqueEmail("cancel-owner-other@test.com"));
        BolsaDeJugador bolsa = saveJugador("Jugador cancel ajeno");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();
        inventarioUsuarioRepository.save(new InventarioUsuario(seller, bolsa, 10, 0));

        UUID anuncioId = mercadoService.vender(seller.getId(), jugadorId, 5).getId();

        assertThrows(SinAutorizacionExpcetion.class,
                () -> mercadoService.cancelarVenta(otro.getId(), anuncioId));
    }

    @Test
    void cancelarVentaFallaCuandoElAnuncioYaFueCancelado() {
        Usuario vendedor = saveUser("Vendedor", uniqueEmail("cancel-twice@test.com"));
        BolsaDeJugador bolsa = saveJugador("Jugador cancel doble");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();
        inventarioUsuarioRepository.save(new InventarioUsuario(vendedor, bolsa, 10, 0));

        UUID anuncioId = mercadoService.vender(vendedor.getId(), jugadorId, 5).getId();
        mercadoService.cancelarVenta(vendedor.getId(), anuncioId);

        assertThrows(AnuncioNoPendienteException.class,
                () -> mercadoService.cancelarVenta(vendedor.getId(), anuncioId));
    }

    @Test
    void cancelarVentaFallaCuandoElAnuncioYaFueCompletado() {
        Usuario vendedor = saveUser("Vendedor", uniqueEmail("cancel-sold-seller@test.com"));
        Usuario comprador = saveUser("Comprador", uniqueEmail("cancel-sold-buyer@test.com"));
        BolsaDeJugador bolsa = saveJugador("Jugador cancel vendido");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();
        inventarioUsuarioRepository.save(new InventarioUsuario(vendedor, bolsa, 10, 0));

        UUID anuncioId = mercadoService.vender(vendedor.getId(), jugadorId, 5).getId();
        mercadoService.comprar(comprador.getId(), anuncioId);

        assertThrows(AnuncioNoPendienteException.class,
                () -> mercadoService.cancelarVenta(vendedor.getId(), anuncioId));
    }

    @Test
    void comprarSuperFallaSiLaCantidadEsNulaCeroONegativa() {
        Usuario buyer = saveUser("Comprador", uniqueEmail("super-qty-buyer@test.com"));
        BolsaDeJugador bolsa = saveJugador("Jugador super cantidad");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();

        assertThrows(CantidadDeTokensException.class,
                () -> mercadoService.comprarSuper(buyer.getId(), jugadorId, null));
        assertThrows(CantidadDeTokensException.class,
                () -> mercadoService.comprarSuper(buyer.getId(), jugadorId, 0));
        assertThrows(CantidadDeTokensException.class,
                () -> mercadoService.comprarSuper(buyer.getId(), jugadorId, -3));
    }

    @Test
    void comprarSuperFallaSiElCompradorNoExiste() {
        BolsaDeJugador bolsa = saveJugador("Jugador super sin comprador");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();

        assertThrows(UsuarioNoEncontradoException.class,
                () -> mercadoService.comprarSuper(UUID.randomUUID(), jugadorId, 1));
    }

    @Test
    void comprarSuperFallaSiElJugadorNoExiste() {
        Usuario buyer = saveUser("Comprador", uniqueEmail("super-noplayer-buyer@test.com"));

        assertThrows(JugadorNoEncontradoException.class,
                () -> mercadoService.comprarSuper(buyer.getId(), UUID.randomUUID(), 1));
    }

    @Test
    void comprarSuperFallaSiLaCantidadSuperaLosTokensRestantes() {
        Usuario buyer = saveUser("Comprador", uniqueEmail("super-exceed-buyer@test.com"));
        buyer.setCreditosDisponibles(BigDecimal.valueOf(100000));
        usuarioRepository.save(buyer);
        BolsaDeJugador bolsa = saveJugador("Jugador super excedido");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();
        int cantidadExcedida = bolsa.getTokensRestantes() + 1;

        assertThrows(CantidadDeTokensException.class,
                () -> mercadoService.comprarSuper(buyer.getId(), jugadorId, cantidadExcedida));
    }

    @Test
    void comprarSuperFallaSiLosCreditosSonInsuficientes() {
        Usuario buyer = saveUser("Comprador", uniqueEmail("super-credits-buyer@test.com"));
        buyer.setCreditosDisponibles(BigDecimal.ZERO);
        usuarioRepository.save(buyer);
        BolsaDeJugador bolsa = saveJugador("Jugador super sin creditos");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();

        assertThrows(CreditosInsuficientesException.class,
                () -> mercadoService.comprarSuper(buyer.getId(), jugadorId, 1));

        // No debe haber cambiado nada
        assertEquals(0, operacionRepository.count());
        assertEquals(0, inventarioUsuarioRepository.count());
    }

    @Test
    void comprarSuperDescuentaCreditosYTokensRestantesYAcreditarInventario() {
        Usuario buyer = saveUser("Comprador", uniqueEmail("super-ok-buyer@test.com"));
        buyer.setCreditosDisponibles(BigDecimal.valueOf(100));
        usuarioRepository.save(buyer);
        BolsaDeJugador bolsa = saveJugador("Jugador super ok");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();
        int tokensIniciales = bolsa.getTokensRestantes();

        mercadoService.comprarSuper(buyer.getId(), jugadorId, 3);

        Usuario compradorActualizado = usuarioRepository.findById(buyer.getId()).orElseThrow();
        BolsaDeJugador bolsaActualizada = jugadorRepository.findById(jugadorId);
        InventarioUsuario inventario = inventarioUsuarioRepository
                .findByUsuarioIdAndJugadorId(buyer.getId(), jugadorId).orElseThrow();

        // 3 tokens * cotización 5 = 15
        assertEquals(0, compradorActualizado.getCreditosDisponibles().compareTo(BigDecimal.valueOf(85)));
        assertEquals(tokensIniciales - 3, bolsaActualizada.getTokensRestantes());
        assertEquals(3, inventario.getTokensDisponibles());
    }

    @Test
    void comprarSuperRegistraLaOperacionDeCompra() {
        Usuario buyer = saveUser("Comprador", uniqueEmail("super-op-buyer@test.com"));
        buyer.setCreditosDisponibles(BigDecimal.valueOf(100));
        usuarioRepository.save(buyer);
        BolsaDeJugador bolsa = saveJugador("Jugador super operacion");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();

        mercadoService.comprarSuper(buyer.getId(), jugadorId, 3);

        List<Operacion> historial = mercadoService.getTransaccionesById(buyer.getId());
        assertEquals(1, historial.size());
        assertEquals(TipoOperacion.COMPRA, historial.get(0).getTipo());
        assertEquals(3, historial.get(0).getCantidad());
        assertEquals(0, historial.get(0).getTotal().compareTo(BigDecimal.valueOf(15)));
        assertEquals(jugadorId, historial.get(0).getJugador().getId());
    }

    @Test
    void comprarSuperSumaTokensAlInventarioExistente() {
        Usuario buyer = saveUser("Comprador", uniqueEmail("super-existing-buyer@test.com"));
        buyer.setCreditosDisponibles(BigDecimal.valueOf(100));
        usuarioRepository.save(buyer);
        BolsaDeJugador bolsa = saveJugador("Jugador super inventario");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();
        inventarioUsuarioRepository.save(new InventarioUsuario(buyer, bolsa, 4, 0));

        mercadoService.comprarSuper(buyer.getId(), jugadorId, 3);

        InventarioUsuario inventario = inventarioUsuarioRepository
                .findByUsuarioIdAndJugadorId(buyer.getId(), jugadorId).orElseThrow();
        assertEquals(7, inventario.getTokensDisponibles());
        assertEquals(1, inventarioUsuarioRepository.count());
    }

    @Test
    void comprarSuperPermiteComprarTodosLosTokensRestantes() {
        Usuario buyer = saveUser("Comprador", uniqueEmail("super-all-buyer@test.com"));
        buyer.setCreditosDisponibles(BigDecimal.valueOf(100000));
        usuarioRepository.save(buyer);
        BolsaDeJugador bolsa = saveJugador("Jugador super todo");
        UUID jugadorId = bolsa.getEstadisticasDeportivas().getJugador().getId();
        int todos = bolsa.getTokensRestantes();

        mercadoService.comprarSuper(buyer.getId(), jugadorId, todos);

        assertEquals(0, jugadorRepository.findById(jugadorId).getTokensRestantes());
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

        return jugadorRepository.save(bolsa);
    }

    @Test
    void obtenerElInvetarioDeUnUsuarioTest() {
        Usuario usuario = saveUser("Usuario", uniqueEmail("inventario-user@test.com"));
        BolsaDeJugador bolsa1 = saveJugador("Jugador inventario 1");
        BolsaDeJugador bolsa2 = saveJugador("Jugador inventario 2");
        inventarioUsuarioRepository.save(new InventarioUsuario(usuario, bolsa1, 5, 0));
        inventarioUsuarioRepository.save(new InventarioUsuario(usuario, bolsa2, 2, 1));

        List<InventarioUsuario> inventario = mercadoService.getInventarioById(usuario.getId());

        assertEquals(2, inventario.size());
    }

    @Test
    void obtenerElInventarioDeUnUsuarioNoTeMuestraElDelResto() {
        Usuario usuario = saveUser("Usuario", uniqueEmail("inventario-own@test.com"));
        Usuario otro = saveUser("Otro", uniqueEmail("inventario-other@test.com"));
        BolsaDeJugador bolsa = saveJugador("Jugador inventario ajeno");
        inventarioUsuarioRepository.save(new InventarioUsuario(usuario, bolsa, 5, 0));
        inventarioUsuarioRepository.save(new InventarioUsuario(otro, bolsa, 9, 0));

        List<InventarioUsuario> inventario = mercadoService.getInventarioById(usuario.getId());

        assertEquals(1, inventario.size());
        assertEquals(5, inventario.getFirst().getTokensDisponibles());
    }

    @Test
    void obtenerElInventarioDeUnUsuarioVacio() {
        Usuario usuario = saveUser("Usuario", uniqueEmail("inventario-empty@test.com"));

        assertTrue(mercadoService.getInventarioById(usuario.getId()).isEmpty());
    }

    @Test
    void consultarUnInventarioFallaSiElUsuarioNoExisteTest() {
        assertThrows(UsuarioNoEncontradoException.class,
                () -> mercadoService.getInventarioById(UUID.randomUUID()));
    }

    private String uniqueEmail(String base) {
        long suffix = EMAIL_SEQUENCE.getAndIncrement();
        return base.replace("@", "+" + suffix + "@");
    }
}
