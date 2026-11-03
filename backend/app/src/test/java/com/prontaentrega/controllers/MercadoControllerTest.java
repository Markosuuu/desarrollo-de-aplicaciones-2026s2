package com.prontaentrega.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prontaentrega.models.*;
import com.prontaentrega.models.enums.EstadoAnuncioVenta;
import com.prontaentrega.models.enums.Liga;
import com.prontaentrega.models.enums.Posicion;
import com.prontaentrega.persistence.repository.AnuncioVentaRepository;
import com.prontaentrega.persistence.repository.HistorialCotizacionRepository;
import com.prontaentrega.persistence.repository.InventarioUsuarioRepository;
import com.prontaentrega.persistence.repository.JugadorRepository;
import com.prontaentrega.persistence.repository.OperacionRepository;
import com.prontaentrega.persistence.repository.UsuarioRepository;
import com.prontaentrega.services.AuthService;
import com.prontaentrega.services.MercadoService;
import com.prontaentrega.services.dto.AuthResult;
import com.prontaentrega.utils.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Tag("integracion")
public class MercadoControllerTest extends AbstractIntegrationTest {
    private static final AtomicInteger JUGADOR_SEQUENCE = new AtomicInteger(1000);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

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

    @Autowired
    private HistorialCotizacionRepository historialCotizacionRepository;

    @BeforeEach
    void limpiarBaseDeDatos() {
        operacionRepository.deleteAll();
        anuncioVentaRepository.deleteAll();
        inventarioUsuarioRepository.deleteAll();
        historialCotizacionRepository.deleteAll();
        usuarioRepository.deleteAll();
        jugadorRepository.deleteAll();
    }

    // GET /api/marketplace/listings

    @Test
    void obtenerAnunciosPendientesConValoresPorDefectoTest() throws Exception {
        UsuarioAutenticado vendedor = registrar("Vendedor", "listings-vendedor@test.com");
        BolsaDeJugador bolsa = crearBolsa("Jugador listings");
        darInventario(vendedor.id(), bolsa, 10);
        publicar(vendedor.id(), bolsa, 2);
        publicar(vendedor.id(), bolsa, 3);

        mockMvc.perform(get("/api/marketplace/listings")
                        .header("Authorization", bearer(vendedor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void obtenerAnunciosDeberiaPaginarSegunPageYSize() throws Exception {
        UsuarioAutenticado vendedor = registrar("Vendedor", "listings-paginado@test.com");
        BolsaDeJugador bolsa = crearBolsa("Jugador paginado");
        darInventario(vendedor.id(), bolsa, 10);
        publicar(vendedor.id(), bolsa, 1);
        publicar(vendedor.id(), bolsa, 1);
        publicar(vendedor.id(), bolsa, 1);

        mockMvc.perform(get("/api/marketplace/listings")
                        .param("page", "1").param("size", "2")
                        .header("Authorization", bearer(vendedor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/marketplace/listings")
                        .param("page", "2").param("size", "2")
                        .header("Authorization", bearer(vendedor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void siNoHayAnunciosObtenerAnunciosNoRetornaNadaTest() throws Exception {
        UsuarioAutenticado usuario = registrar("Usuario", "listings-vacio@test.com");

        mockMvc.perform(get("/api/marketplace/listings")
                        .header("Authorization", bearer(usuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void sinTokenObtenerAnunciosNoFunciona() throws Exception {
        mockMvc.perform(get("/api/marketplace/listings"))
                .andExpect(status().isForbidden());
    }

    @Test
    void conTokenInvalidoObtenerAnunciosNoFunciona() throws Exception {
        mockMvc.perform(get("/api/marketplace/listings")
                        .header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());
    }

    // POST /api/orders/sell

    @Test
    void venderDeberiaCrearElAnuncioYDevolver201() throws Exception {
        UsuarioAutenticado vendedor = registrar("Vendedor", "sell-ok@test.com");
        BolsaDeJugador bolsa = crearBolsa("Jugador sell");
        UUID jugadorId = jugadorId(bolsa);
        darInventario(vendedor.id(), bolsa, 10);

        mockMvc.perform(post("/api/orders/sell")
                        .header("Authorization", bearer(vendedor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("jugadorId", jugadorId, "cantidad", 5))))
                .andExpect(status().isCreated());

        assertEquals(1, anuncioVentaRepository.count());
        InventarioUsuario inventario = inventarioUsuarioRepository
                .findByUsuarioIdAndJugadorId(vendedor.id(), jugadorId).orElseThrow();
        assertEquals(5, inventario.getTokensDisponibles());
        assertEquals(5, inventario.getTokensEnVenta());
    }

    @Test
    void venderDeberiaRechazarCantidadMayorALaDisponible() throws Exception {
        UsuarioAutenticado vendedor = registrar("Vendedor", "sell-exceso@test.com");
        BolsaDeJugador bolsa = crearBolsa("Jugador sell exceso");
        darInventario(vendedor.id(), bolsa, 3);

        mockMvc.perform(post("/api/orders/sell")
                        .header("Authorization", bearer(vendedor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("jugadorId", jugadorId(bolsa), "cantidad", 5))))
                .andExpect(status().isBadRequest());

        assertEquals(0, anuncioVentaRepository.count());
    }

    @Test
    void venderDeberiaRechazarSinToken() throws Exception {
        mockMvc.perform(post("/api/orders/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("jugadorId", UUID.randomUUID(), "cantidad", 1))))
                .andExpect(status().isForbidden());
    }

    // POST /api/orders/buy

    @Test
    void comprarUnAnuncioTest() throws Exception {
        UsuarioAutenticado vendedor = registrar("Vendedor", "buy-total-vendedor@test.com");
        UsuarioAutenticado comprador = registrar("Comprador", "buy-total-comprador@test.com");
        darCreditos(comprador.id(), 1000);
        BolsaDeJugador bolsa = crearBolsa("Jugador buy total");
        UUID jugadorId = jugadorId(bolsa);
        darInventario(vendedor.id(), bolsa, 10);
        UUID anuncioId = publicar(vendedor.id(), bolsa, 5);

        mockMvc.perform(post("/api/orders/buy")
                        .header("Authorization", bearer(comprador))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("anuncioId", anuncioId, "cantidad", 5))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Compra completada correctamente"));

        InventarioUsuario tokensComprador = inventarioUsuarioRepository
                .findByUsuarioIdAndJugadorId(comprador.id(), jugadorId).orElseThrow();
        assertEquals(5, tokensComprador.getTokensDisponibles());
        assertEquals(EstadoAnuncioVenta.COMPLETADA,
                anuncioVentaRepository.findById(anuncioId).orElseThrow().getEstado());
    }

    @Test
    void comprarParcialmenteDejaElAnuncioPendienteYCompraAlmismoTiempo() throws Exception {
        UsuarioAutenticado vendedor = registrar("Vendedor", "buy-parcial-vendedor@test.com");
        UsuarioAutenticado comprador = registrar("Comprador", "buy-parcial-comprador@test.com");
        darCreditos(comprador.id(), 1000);
        BolsaDeJugador bolsa = crearBolsa("Jugador buy parcial");
        UUID jugadorId = jugadorId(bolsa);
        darInventario(vendedor.id(), bolsa, 10);
        UUID anuncioId = publicar(vendedor.id(), bolsa, 5);

        mockMvc.perform(post("/api/orders/buy")
                        .header("Authorization", bearer(comprador))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("anuncioId", anuncioId, "cantidad", 2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        AnuncioVenta anuncio = anuncioVentaRepository.findById(anuncioId).orElseThrow();
        assertEquals(EstadoAnuncioVenta.PENDIENTE, anuncio.getEstado());
        assertEquals(3, anuncio.getCantidadTokens());
        assertEquals(2, inventarioUsuarioRepository
                .findByUsuarioIdAndJugadorId(comprador.id(), jugadorId).orElseThrow()
                .getTokensDisponibles());
    }

    @Test
    void comprarDeberiaRechazarCantidadMayorALaDelAnuncio() throws Exception {
        UsuarioAutenticado vendedor = registrar("Vendedor", "buy-rango-vendedor@test.com");
        UsuarioAutenticado comprador = registrar("Comprador", "buy-rango-comprador@test.com");
        darCreditos(comprador.id(), 1000);
        BolsaDeJugador bolsa = crearBolsa("Jugador buy rango");
        darInventario(vendedor.id(), bolsa, 10);
        UUID anuncioId = publicar(vendedor.id(), bolsa, 3);

        mockMvc.perform(post("/api/orders/buy")
                        .header("Authorization", bearer(comprador))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("anuncioId", anuncioId, "cantidad", 4))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void comprarDeberiaRechazarLaCompraDeTokensPropios() throws Exception {
        UsuarioAutenticado vendedor = registrar("Vendedor", "buy-propio@test.com");
        BolsaDeJugador bolsa = crearBolsa("Jugador buy propio");
        darInventario(vendedor.id(), bolsa, 10);
        UUID anuncioId = publicar(vendedor.id(), bolsa, 3);

        mockMvc.perform(post("/api/orders/buy")
                        .header("Authorization", bearer(vendedor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("anuncioId", anuncioId, "cantidad", 1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VENDEDOR_COMPRANDO_TOKENS_PROPIOS"));
    }

    @Test
    void comprarDeberiaRechazarSiLosCreditosSonInsuficientes() throws Exception {
        UsuarioAutenticado vendedor = registrar("Vendedor", "buy-creditos-vendedor@test.com");
        UsuarioAutenticado comprador = registrar("Comprador", "buy-creditos-comprador@test.com");
        darCreditos(comprador.id(), 0);
        BolsaDeJugador bolsa = crearBolsa("Jugador buy creditos");
        darInventario(vendedor.id(), bolsa, 10);
        UUID anuncioId = publicar(vendedor.id(), bolsa, 3);

        mockMvc.perform(post("/api/orders/buy")
                        .header("Authorization", bearer(comprador))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("anuncioId", anuncioId, "cantidad", 3))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("CREDITOS_INSUFICIENTES_PARA_COMPRAR"));
    }

    @Test
    void comprarDeberiaRechazarUnAnuncioInexistente() throws Exception {
        UsuarioAutenticado comprador = registrar("Comprador", "buy-inexistente@test.com");

        mockMvc.perform(post("/api/orders/buy")
                        .header("Authorization", bearer(comprador))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("anuncioId", UUID.randomUUID(), "cantidad", 1))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("ANUNCIO_NO_ENCONTRADO"));
    }

    @Test
    void comprarDeberiaRechazarUnAnuncioQueYaNoEstaPendiente() throws Exception {
        UsuarioAutenticado vendedor = registrar("Vendedor", "buy-cerrado-vendedor@test.com");
        UsuarioAutenticado comprador1 = registrar("Comprador 1", "buy-cerrado-c1@test.com");
        UsuarioAutenticado comprador2 = registrar("Comprador 2", "buy-cerrado-c2@test.com");
        darCreditos(comprador1.id(), 1000);
        darCreditos(comprador2.id(), 1000);
        BolsaDeJugador bolsa = crearBolsa("Jugador buy cerrado");
        darInventario(vendedor.id(), bolsa, 10);
        UUID anuncioId = publicar(vendedor.id(), bolsa, 3);
        mercadoService.comprar(comprador1.id(), anuncioId);

        mockMvc.perform(post("/api/orders/buy")
                        .header("Authorization", bearer(comprador2))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("anuncioId", anuncioId, "cantidad", 1))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("ANUNCIO_NO_PENDIENTE"));
    }

    @Test
    void comprarDeberiaRechazarCantidadCero() throws Exception {
        UsuarioAutenticado comprador = registrar("Comprador", "buy-cero@test.com");

        mockMvc.perform(post("/api/orders/buy")
                        .header("Authorization", bearer(comprador))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("anuncioId", UUID.randomUUID(), "cantidad", 0))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void comprarDeberiaRechazarUnBodySinCantidad() throws Exception {
        UsuarioAutenticado comprador = registrar("Comprador", "buy-sin-cantidad@test.com");

        mockMvc.perform(post("/api/orders/buy")
                        .header("Authorization", bearer(comprador))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("anuncioId", UUID.randomUUID()))))
                .andExpect(status().isBadRequest());
    }

    //  DELETE /api/orders/{anuncioId}

    @Test
    void cancelarVentaDeberiaCancelarElAnuncioYDevolverLosTokens() throws Exception {
        UsuarioAutenticado vendedor = registrar("Vendedor", "cancel-ok@test.com");
        BolsaDeJugador bolsa = crearBolsa("Jugador cancel");
        UUID jugadorId = jugadorId(bolsa);
        darInventario(vendedor.id(), bolsa, 10);
        UUID anuncioId = publicar(vendedor.id(), bolsa, 5);

        mockMvc.perform(delete("/api/orders/{anuncioId}", anuncioId)
                        .header("Authorization", bearer(vendedor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Anuncio cancelado correctamente"));

        assertEquals(EstadoAnuncioVenta.CANCELADA,
                anuncioVentaRepository.findById(anuncioId).orElseThrow().getEstado());
        InventarioUsuario inventario = inventarioUsuarioRepository
                .findByUsuarioIdAndJugadorId(vendedor.id(), jugadorId).orElseThrow();
        assertEquals(10, inventario.getTokensDisponibles());
        assertEquals(0, inventario.getTokensEnVenta());
    }

    @Test
    void cancelarVentaDeberiaRechazarSiElUsuarioNoEsElVendedor() throws Exception {
        UsuarioAutenticado vendedor = registrar("Vendedor", "cancel-ajeno-vendedor@test.com");
        UsuarioAutenticado otro = registrar("Otro", "cancel-ajeno-otro@test.com");
        BolsaDeJugador bolsa = crearBolsa("Jugador cancel ajeno");
        darInventario(vendedor.id(), bolsa, 10);
        UUID anuncioId = publicar(vendedor.id(), bolsa, 5);

        mockMvc.perform(delete("/api/orders/{anuncioId}", anuncioId)
                        .header("Authorization", bearer(otro)))
                .andExpect(status().isForbidden());

        assertEquals(EstadoAnuncioVenta.PENDIENTE,
                anuncioVentaRepository.findById(anuncioId).orElseThrow().getEstado());
    }

    @Test
    void cancelarVentaDeberiaRechazarUnAnuncioInexistente() throws Exception {
        UsuarioAutenticado vendedor = registrar("Vendedor", "cancel-inexistente@test.com");

        mockMvc.perform(delete("/api/orders/{anuncioId}", UUID.randomUUID())
                        .header("Authorization", bearer(vendedor)))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelarVentaDeberiaRechazarSinToken() throws Exception {
        mockMvc.perform(delete("/api/orders/{anuncioId}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    // GET /api/users/{id}/transactions

    @Test
    void obtenerElHistorialDelUsuario() throws Exception {
        UsuarioAutenticado vendedor = registrar("Vendedor", "tx-vendedor@test.com");
        UsuarioAutenticado comprador = registrar("Comprador", "tx-comprador@test.com");
        darCreditos(comprador.id(), 1000);
        BolsaDeJugador bolsa = crearBolsa("Jugador transacciones");
        darInventario(vendedor.id(), bolsa, 10);
        UUID anuncioId = publicar(vendedor.id(), bolsa, 3);
        mercadoService.comprar(comprador.id(), anuncioId);

        mockMvc.perform(get("/api/users/{id}/transactions", vendedor.id())
                        .header("Authorization", bearer(vendedor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void siNoHayOperaciones_obtenerElHistorialEsVacio() throws Exception {
        UsuarioAutenticado usuario = registrar("Usuario", "tx-vacio@test.com");

        mockMvc.perform(get("/api/users/{id}/transactions", usuario.id())
                        .header("Authorization", bearer(usuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void sinTokenFallaConsultarElHistorialTest() throws Exception {
        mockMvc.perform(get("/api/users/{id}/transactions", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    // POST /api/orders/super/buy

    @Test
    void comprarASuperRetornaElDetalle() throws Exception {
        UsuarioAutenticado comprador = registrar("Comprador", "super-ok@test.com");
        darCreditos(comprador.id(), 1000);
        BolsaDeJugador bolsa = crearBolsa("Jugador super");
        UUID jugadorId = jugadorId(bolsa);

        mockMvc.perform(post("/api/orders/super/buy")
                        .header("Authorization", bearer(comprador))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("jugadorId", jugadorId, "cantidad", 3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Compra completada correctamente"))
                .andExpect(jsonPath("$.jugadorId").value(jugadorId.toString()))
                .andExpect(jsonPath("$.cantidad").value(3));

        assertEquals(3, inventarioUsuarioRepository
                .findByUsuarioIdAndJugadorId(comprador.id(), jugadorId).orElseThrow()
                .getTokensDisponibles());
    }

    @Test
    void comprarASuperDeberiaRechazarUnaCantidadInvalida() throws Exception {
        UsuarioAutenticado comprador = registrar("Comprador", "super-cantidad@test.com");
        BolsaDeJugador bolsa = crearBolsa("Jugador super cantidad");

        mockMvc.perform(post("/api/orders/super/buy")
                        .header("Authorization", bearer(comprador))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("jugadorId", jugadorId(bolsa), "cantidad", 0))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void comprarSuperDeberiaRechazarUnJugadorInexistente() throws Exception {
        UsuarioAutenticado comprador = registrar("Comprador", "super-jugador@test.com");

        mockMvc.perform(post("/api/orders/super/buy")
                        .header("Authorization", bearer(comprador))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("jugadorId", UUID.randomUUID(), "cantidad", 1))))
                .andExpect(status().isNotFound());
    }

    @Test
    void comprarSuperDeberiaRechazarSiLosCreditosSonInsuficientes() throws Exception {
        UsuarioAutenticado comprador = registrar("Comprador", "super-creditos@test.com");
        darCreditos(comprador.id(), 0);
        BolsaDeJugador bolsa = crearBolsa("Jugador super creditos");

        mockMvc.perform(post("/api/orders/super/buy")
                        .header("Authorization", bearer(comprador))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("jugadorId", jugadorId(bolsa), "cantidad", 1))))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void comprarSuperDeberiaRechazarSinToken() throws Exception {
        mockMvc.perform(post("/api/orders/super/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("jugadorId", UUID.randomUUID(), "cantidad", 1))))
                .andExpect(status().isForbidden());
    }

    // GET /api/users/{id}/inventario

    @Test
    void obtenerElInventarioDeUnUsuarioTest() throws Exception {
        UsuarioAutenticado usuario = registrar("Usuario", "inv-ok@test.com");
        darInventario(usuario.id(), crearBolsa("Jugador inv 1"), 5);
        darInventario(usuario.id(), crearBolsa("Jugador inv 2"), 2);

        mockMvc.perform(get("/api/users/{id}/inventario", usuario.id())
                        .header("Authorization", bearer(usuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void siElUsuarioNoTieneTokensObtenerElInventarioEsVacio() throws Exception {
        UsuarioAutenticado usuario = registrar("Usuario", "inv-vacio@test.com");

        mockMvc.perform(get("/api/users/{id}/inventario", usuario.id())
                        .header("Authorization", bearer(usuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void obtenerInventarioDeberiaRechazarUnUsuarioInexistente() throws Exception {
        UsuarioAutenticado usuario = registrar("Usuario", "inv-inexistente@test.com");

        mockMvc.perform(get("/api/users/{id}/inventario", UUID.randomUUID())
                        .header("Authorization", bearer(usuario)))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerInventarioDeberiaRechazarSinToken() throws Exception {
        mockMvc.perform(get("/api/users/{id}/inventario", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    // helpers

    private record UsuarioAutenticado(UUID id, String token) {
    }

    private UsuarioAutenticado registrar(String nombre, String correo) {
        AuthResult resultado = authService.register(nombre, correo, "abcd1234");
        return new UsuarioAutenticado(resultado.usuario().getId(), resultado.token());
    }

    private String bearer(UsuarioAutenticado usuario) {
        return "Bearer " + usuario.token();
    }

    private String json(Map<String, Object> campos) throws Exception {
        return objectMapper.writeValueAsString(campos);
    }

    private void darCreditos(UUID usuarioId, int creditos) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        usuario.setCreditosDisponibles(BigDecimal.valueOf(creditos));
        usuarioRepository.save(usuario);
    }

    private void darInventario(UUID usuarioId, BolsaDeJugador bolsa, int tokens) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        inventarioUsuarioRepository.save(new InventarioUsuario(usuario, bolsa, tokens, 0));
    }

    private UUID publicar(UUID vendedorId, BolsaDeJugador bolsa, int cantidad) {
        return mercadoService.vender(vendedorId, jugadorId(bolsa), cantidad).getId();
    }

    private UUID jugadorId(BolsaDeJugador bolsa) {
        return bolsa.getEstadisticasDeportivas().getJugador().getId();
    }

    private BolsaDeJugador crearBolsa(String nombre) {
        Jugador jugador = new Jugador(JUGADOR_SEQUENCE.getAndIncrement(),
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
}