package com.prontaentrega.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prontaentrega.models.*;
import com.prontaentrega.models.enums.Liga;
import com.prontaentrega.models.enums.Posicion;
import com.prontaentrega.persistence.repository.InventarioUsuarioRepository;
import com.prontaentrega.persistence.repository.JugadorRepository;
import com.prontaentrega.persistence.repository.OfertaRepository;
import com.prontaentrega.persistence.repository.OperacionRepository;
import com.prontaentrega.persistence.repository.SubastaRepository;
import com.prontaentrega.persistence.repository.UsuarioRepository;
import com.prontaentrega.services.AuthService;
import com.prontaentrega.services.dto.AuthResult;
import com.prontaentrega.utils.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Tag("integracion")
class SubastaControllerTest extends AbstractIntegrationTest {
    private static final AtomicInteger PLAYER_SEQUENCE = new AtomicInteger(10000);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

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
    void shouldCreateAuctionWithValidData() throws Exception {
        AuthenticatedUser seller = register("Seller", "auction-seller@test.com");

        UUID auctionId = createAuction(seller);

        Subasta auction = subastaRepository.findById(auctionId).orElseThrow();

        assertTrue(auction.getActiva());
        assertEquals(2, auction.getCantidadOfrecida());
        assertNull(auction.getOfertaActual());
    }

    @Test
    void shouldAcceptValidBid() throws Exception {
        AuthenticatedUser seller = register("Seller", "seller-bid@test.com");
        AuthenticatedUser buyer = register("Buyer", "buyer-bid@test.com");

        UUID auctionId = createAuction(seller);
        configureCredits(buyer, 500);

        ofertar(buyer, auctionId, 80, "buyer-bid-1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(true));
    }

    @Test
    void shouldHandleRepeatedBidIdempotently() throws Exception {
        AuthenticatedUser seller = register("Seller", "seller-idempotent@test.com");
        AuthenticatedUser buyer = register("Buyer", "buyer-idempotent@test.com");

        UUID auctionId = createAuction(seller);
        configureCredits(buyer, 500);

        ofertar(buyer, auctionId, 80, "buyer-auction-1")
                .andExpect(status().isOk());

        ofertar(buyer, auctionId, 80, "buyer-auction-1")
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/subastas/{id}", auctionId)
                        .header("Authorization", bearer(buyer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ofertasPrevias.length()").value(1));
    }

    @Test
    void shouldRejectDifferentBidWithSameIdempotencyKey() throws Exception {
        AuthenticatedUser seller = register("Seller", "seller-conflict@test.com");
        AuthenticatedUser buyer = register("Buyer", "buyer-conflict@test.com");

        UUID auctionId = createAuction(seller);
        configureCredits(buyer, 500);

        ofertar(buyer, auctionId, 80, "buyer-auction-1")
                .andExpect(status().isOk());

        ofertar(buyer, auctionId, 90, "buyer-auction-1")
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnAuctionDetailsWithPreviousBids() throws Exception {
        AuthenticatedUser seller = register("Seller", "seller-detail@test.com");
        AuthenticatedUser buyer = register("Buyer", "buyer-detail@test.com");

        UUID auctionId = createAuction(seller);
        configureCredits(buyer, 500);

        ofertar(buyer, auctionId, 80, "buyer-detail-1")
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/subastas/{id}", auctionId)
                        .header("Authorization", bearer(buyer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ofertaActual").value(80))
                .andExpect(jsonPath("$.ofertasPrevias.length()").value(1));
    }

    @Test
    void shouldCloseAuctionWithWinner() throws Exception {
        AuthenticatedUser seller = register("Seller", "seller-close@test.com");
        AuthenticatedUser buyer = register("Buyer", "buyer-close@test.com");

        UUID auctionId = createAuction(seller);
        configureCredits(buyer, 500);

        ofertar(buyer, auctionId, 80, "buyer-close-1")
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/subastas/{id}/cerrar", auctionId)
                        .header("Authorization", bearer(seller))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("motivo", "POR_PROPIETARIO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ganadorId").value(buyer.id().toString()))
                .andExpect(jsonPath("$.montoFinal").value(80))
                .andExpect(jsonPath("$.resultado").value("VENTA"));
    }

    @Test
    void shouldRejectBidOnClosedAuction() throws Exception {
        AuthenticatedUser seller = register("Seller", "seller-closed@test.com");
        AuthenticatedUser buyer = register("Buyer", "buyer-closed@test.com");

        UUID auctionId = createAuction(seller);
        configureCredits(buyer,500);

        ofertar(buyer, auctionId, 80, "buyer-closed-1")
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/subastas/{id}/cerrar", auctionId)
                        .header("Authorization", bearer(seller))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("motivo", "POR_PROPIETARIO"))))
                .andExpect(status().isOk());

        ofertar(buyer, auctionId, 90, "buyer-closed-2")
                .andExpect(status().isConflict());
    }


    @Test
    void shouldReturnEmptyOffersWhenAuctionHasNoBids() throws Exception {
        AuthenticatedUser seller = register("Seller", "seller-offers@test.com");
        BolsaDeJugador player = createPlayer();

        UUID playerId = player.getEstadisticasDeportivas().getJugador().getId();
        Usuario sellerModel = usuarioRepository.findById(seller.id()).orElseThrow();

        inventarioUsuarioRepository.save(new InventarioUsuario(sellerModel, player, 5, 0));

        String response = mockMvc.perform(post("/api/subastas")
                        .header("Authorization", bearer(seller))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "jugadorId", playerId,
                                "cantidad", 2,
                                "precioInicial", 50,
                                "vencimiento", LocalDateTime.now().plusHours(2)
                                        .withSecond(0).withNano(0).toString()
                        ))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID auctionId = UUID.fromString(objectMapper.readTree(response).get("id").asText());

        mockMvc.perform(get("/api/subastas/{id}/ofertas", auctionId)
                        .header("Authorization", bearer(seller)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnOffersWhenAuctionHasBids() throws Exception {
        AuthenticatedUser seller = register("Seller", "seller-bids@test.com");
        AuthenticatedUser buyer = register("Buyer", "buyer-bids@test.com");

        BolsaDeJugador player = createPlayer();
        UUID playerId = player.getEstadisticasDeportivas().getJugador().getId();

        Usuario sellerModel = usuarioRepository.findById(seller.id()).orElseThrow();
        Usuario buyerModel = usuarioRepository.findById(buyer.id()).orElseThrow();

        buyerModel.setCreditosDisponibles(BigDecimal.valueOf(500));
        usuarioRepository.save(buyerModel);

        inventarioUsuarioRepository.save(new InventarioUsuario(sellerModel, player, 5, 0));

        String response = mockMvc.perform(post("/api/subastas")
                        .header("Authorization", bearer(seller))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "jugadorId", playerId,
                                "cantidad", 2,
                                "precioInicial", 50,
                                "vencimiento", LocalDateTime.now().plusHours(2)
                                        .withSecond(0).withNano(0).toString()
                        ))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID auctionId = UUID.fromString(objectMapper.readTree(response).get("id").asText());

        mockMvc.perform(post("/api/subastas/ofertar")
                        .header("Authorization", bearer(buyer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "subastaId", auctionId,
                                "puja", 80,
                                "idempotencyKey", "buyer-bid-1"
                        ))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/subastas/{id}/ofertas", auctionId)
                        .header("Authorization", bearer(seller)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldListAuctionsWhenSearchFiltersAreEmpty() throws Exception {
        AuthenticatedUser seller = register("Seller", "seller-search@test.com");

        mockMvc.perform(post("/api/subastas/buscarPorNombre")
                        .header("Authorization", bearer(seller))
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "user", "",
                                "jugador", ""
                        ))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldListAuctionsFilteredByUserName() throws Exception {
        AuthenticatedUser seller = register("Seller", "seller-filter@test.com");

        BolsaDeJugador player = createPlayer();
        UUID playerId = player.getEstadisticasDeportivas().getJugador().getId();

        Usuario sellerModel = usuarioRepository.findById(seller.id()).orElseThrow();

        inventarioUsuarioRepository.save(new InventarioUsuario(sellerModel, player, 5, 0));

        mockMvc.perform(post("/api/subastas")
                        .header("Authorization", bearer(seller))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "jugadorId", playerId,
                                "cantidad", 2,
                                "precioInicial", 50,
                                "vencimiento", LocalDateTime.now().plusHours(2)
                                        .withSecond(0).withNano(0).toString()
                        ))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/subastas/buscarPorNombre")
                        .header("Authorization", bearer(seller))
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "user", "Seller",
                                "jugador", ""
                        ))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    private AuthenticatedUser register(String name, String email) {
        AuthResult result = authService.register(name, email, "abcd1234");
        return new AuthenticatedUser(result.usuario().getId(), result.token());
    }

    private BolsaDeJugador createPlayer() {
        Jugador jugador = new Jugador(
                PLAYER_SEQUENCE.getAndIncrement(),
                "Jugador subasta",
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
                100);
        return jugadorRepository.save(bolsa);
    }

    private String bearer(AuthenticatedUser user) {
        return "Bearer " + user.token();
    }

    private String json(Map<String, Object> values) throws Exception {
        return objectMapper.writeValueAsString(values);
    }


    private UUID createAuction(AuthenticatedUser seller) throws Exception {
        BolsaDeJugador player = createPlayer();

        UUID playerId = player.getEstadisticasDeportivas()
                .getJugador().getId();

        Usuario sellerModel = usuarioRepository.findById(seller.id())
                .orElseThrow();

        inventarioUsuarioRepository.save(
                new InventarioUsuario(sellerModel, player, 5, 0)
        );

        LocalDateTime deadline = LocalDateTime.now()
                .plusHours(2)
                .withSecond(0)
                .withNano(0);

        String response = mockMvc.perform(post("/api/subastas")
                        .header("Authorization", bearer(seller))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "jugadorId", playerId,
                                "cantidad", 2,
                                "precioInicial", 50,
                                "vencimiento", deadline.toString()
                        ))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(
                objectMapper.readTree(response).get("id").asText()
        );
    }

    private void configureCredits(
            AuthenticatedUser buyer,
            int credits
    ) {
        Usuario buyerModel = usuarioRepository.findById(buyer.id())
                .orElseThrow();

        buyerModel.setCreditosDisponibles(new BigDecimal(credits));
        usuarioRepository.save(buyerModel);
    }

    private ResultActions ofertar(
            AuthenticatedUser buyer,
            UUID auctionId,
            int amount,
            String idempotencyKey
    ) throws Exception {
        return mockMvc.perform(post("/api/subastas/ofertar")
                .header("Authorization", bearer(buyer))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                        "subastaId", auctionId,
                        "puja", amount,
                        "idempotencyKey", idempotencyKey
                ))));
    }


    private record AuthenticatedUser(UUID id, String token) {
    }
}
