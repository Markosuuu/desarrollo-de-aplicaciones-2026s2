package com.prontaentrega.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prontaentrega.models.BolsaDeJugador;
import com.prontaentrega.models.EstadisticasDeportivas;
import com.prontaentrega.models.InventarioUsuario;
import com.prontaentrega.models.Jugador;
import com.prontaentrega.models.Usuario;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void shouldCreateBidInspectAndCloseAuctionThroughApi() throws Exception {
        AuthenticatedUser seller = register("Seller", "auction-seller@test.com");
        AuthenticatedUser buyer = register("Buyer", "auction-buyer@test.com");
        BolsaDeJugador player = createPlayer();
        UUID playerId = player.getEstadisticasDeportivas().getJugador().getId();
        Usuario sellerModel = usuarioRepository.findById(seller.id()).orElseThrow();
        Usuario buyerModel = usuarioRepository.findById(buyer.id()).orElseThrow();
        buyerModel.setCreditosDisponibles(BigDecimal.valueOf(500));
        usuarioRepository.save(buyerModel);
        inventarioUsuarioRepository.save(new InventarioUsuario(sellerModel, player, 5, 0));

        LocalDateTime deadline = LocalDateTime.now().plusHours(2).withSecond(0).withNano(0);
        String createResponse = mockMvc.perform(post("/api/subastas")
                        .header("Authorization", bearer(seller))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "jugadorId", playerId,
                                "cantidad", 2,
                                "precioInicial", 50,
                                "vencimiento", deadline.toString()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.activa").value(true))
                .andReturn().getResponse().getContentAsString();
        JsonNode created = objectMapper.readTree(createResponse);
        UUID auctionId = UUID.fromString(created.get("id").asText());

        mockMvc.perform(post("/api/subastas/ofertar")
                        .header("Authorization", bearer(buyer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "subastaId", auctionId,
                                "puja", 80,
                                "idempotencyKey", "buyer-auction-1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(true));

        mockMvc.perform(get("/api/subastas/{id}", auctionId)
                        .header("Authorization", bearer(buyer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ofertaActual").value(80))
                .andExpect(jsonPath("$.ofertasPrevias.length()").value(1));

        mockMvc.perform(patch("/api/subastas/{id}/cerrar", auctionId)
                        .header("Authorization", bearer(seller))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("motivo", "POR_PROPIETARIO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ganadorId").value(buyer.id().toString()))
                .andExpect(jsonPath("$.montoFinal").value(80))
                .andExpect(jsonPath("$.resultado").value("VENTA"));

        mockMvc.perform(post("/api/subastas/ofertar")
                        .header("Authorization", bearer(buyer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "subastaId", auctionId,
                                "puja", 90,
                                "idempotencyKey", "buyer-auction-2"))))
                .andExpect(status().isConflict());
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

    private record AuthenticatedUser(UUID id, String token) {
    }
}
