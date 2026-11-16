package com.prontaentrega.services;

import com.prontaentrega.models.BolsaDeJugador;
import com.prontaentrega.models.EstadisticasDeportivas;
import com.prontaentrega.models.Jugador;
import com.prontaentrega.models.enums.Liga;
import com.prontaentrega.models.enums.Posicion;
import com.prontaentrega.persistence.repository.HistorialCotizacionRepository;
import com.prontaentrega.persistence.repository.JugadorRepository;
import com.prontaentrega.utils.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@ActiveProfiles("test")
@SpringBootTest
@Tag("integracion")
public class HistorialCotizacionServiceTest extends AbstractIntegrationTest {
    @Autowired
    private HistorialCotizacionService historialCotizacionService;
    @Autowired
    private JugadorRepository jugadorRepository;
    @Autowired
    private HistorialCotizacionRepository historialCotizacionRepository;

    @BeforeEach
    public void clean() {
        historialCotizacionRepository.deleteAll();
        jugadorRepository.deleteAll();
        Jugador jugador = new Jugador(
                1,
                "Messi",
                "Argentina",
                Liga.LALIGA,
                Posicion.MIDFIELDER,
                25,
                180,
                75
        );

        EstadisticasDeportivas estadisticas = new EstadisticasDeportivas(
                jugador,
                true,
                10,
                5,
                BigDecimal.valueOf(2.5),
                BigDecimal.valueOf(1.2),
                BigDecimal.valueOf(1.5),
                BigDecimal.valueOf(0.5),
                BigDecimal.valueOf(7.5),
                2000
        );
        jugadorRepository.save(BolsaDeJugador.desdeEstadisticas(estadisticas, LocalDateTime.now()));
    }
}
