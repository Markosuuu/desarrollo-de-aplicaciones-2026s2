package com.prontaentrega.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.prontaentrega.models.cotizacion.ConfiguracionEstrategiaCotizacion;
import com.prontaentrega.models.cotizacion.HistorialCotizacion;
import com.prontaentrega.models.cotizacion.estrategias.ConfiguracionEstrategiaPonderada;
import com.prontaentrega.models.cotizacion.EstadoEstrategiaCotizacion;
import com.prontaentrega.models.cotizacion.estrategias.ConfiguracionEstrategiaPonderadaPenalizacion;
import com.prontaentrega.models.cotizacion.estrategias.EstrategiaPonderada;
import com.prontaentrega.models.cotizacion.estrategias.EstrategiaPonderadaConPenalizacion;
import com.prontaentrega.models.jugadores.*;
import com.prontaentrega.persistence.repository.CotizacionRepository;
import com.prontaentrega.persistence.repository.HistorialCotizacionRepository;
import com.prontaentrega.persistence.repository.JugadorRepository;
import com.prontaentrega.services.dto.ResultadoRecotizacion;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.prontaentrega.services.exceptions.HistorialNoEncontradoException;
import com.prontaentrega.services.exceptions.cotizacion.ConfiguracionIncompletaException;
import com.prontaentrega.services.exceptions.cotizacion.EstrategiaActivaNoDisponibleException;
import com.prontaentrega.services.exceptions.cotizacion.NoHayEstrategiaActivaConfiguradaException;
import com.prontaentrega.services.exceptions.jugadores.JugadorNoEncontradoException;
import com.prontaentrega.utils.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/** Verifica la coordinación administrativa sin cargar base de datos. */
@ExtendWith(MockitoExtension.class)
@Tag("integracion")
@SpringBootTest
@Transactional
class CotizacionServiceTest extends AbstractIntegrationTest {
    @Mock
    private CotizacionRepository strategyRepository;
    @Autowired
    private HistorialCotizacionRepository historialRepository;
    @Autowired
    private JugadorRepository jugadorRepository;

    private CotizacionService administrationService;

    private BolsaDeJugador jugador;
    private LocalDate fechaBuscada;

    /** Registra las dos estrategias y valida su asociación tipada con los providers. */
    @BeforeEach
    void crearServicio() {
        jugadorRepository.deleteAll();
        historialRepository.deleteAll();

        when(strategyRepository.estrategiaIdsConfigurables())
                .thenReturn(java.util.Set.of("ponderada", "ponderada-con-penalizacion"));
        doReturn(ConfiguracionEstrategiaPonderada.class)
                .when(strategyRepository).tipoConfiguracion("ponderada");
        doReturn(ConfiguracionEstrategiaPonderadaPenalizacion.class)
                .when(strategyRepository).tipoConfiguracion("ponderada-con-penalizacion");
        when(strategyRepository.estrategiaIdsConfigurables())
                .thenReturn(java.util.Set.of("ponderada", "ponderada-con-penalizacion"));
        administrationService = new CotizacionService(strategyRepository, historialRepository, jugadorRepository,
                List.of(new EstrategiaPonderada(), new EstrategiaPonderadaConPenalizacion()));

        jugador = crearJugador();
        fechaBuscada = LocalDate.of(2026, 11, 10);
    }

    /** Actualizar una configuración no selecciona ni recalcula la estrategia. */
    @Test
    void actualizaConfiguracionSinIniciarRecotizacion() {
        ConfiguracionEstrategiaPonderada config = configuracionPonderada();
        when(strategyRepository.bloquearEstado())
                .thenReturn(Optional.of(new EstadoEstrategiaCotizacion(null)));

        ConfiguracionEstrategiaCotizacion guardada =
                administrationService.actualizarConfiguracion("ponderada", config);

        assertThat(guardada).isSameAs(config);
        verify(strategyRepository).guardarConfiguracion("ponderada", config);
        verify(strategyRepository, never()).buscarPagina(null, 200);
    }

    /** Activar persiste primero la selección y luego recotiza con la estrategia seleccionada. */
    @Test
    void activaEstrategiaYRecotizaEnElMismoFlujo() {
        ConfiguracionEstrategiaPonderada config = configuracionPonderada();
        when(strategyRepository.bloquearEstado())
                .thenReturn(Optional.of(new EstadoEstrategiaCotizacion(null)));
        when(strategyRepository.buscarConfiguracion("ponderada")).thenReturn(Optional.of(config));
        when(strategyRepository.bloquearEstado())
                .thenReturn(Optional.of(new EstadoEstrategiaCotizacion(null)))
                .thenReturn(Optional.of(new EstadoEstrategiaCotizacion("ponderada")));
        when(strategyRepository.buscarPagina(null, 200)).thenReturn(List.of());
        ResultadoRecotizacion resultado = new ResultadoRecotizacion("ponderada", 0, 0);

        assertThat(administrationService.activarEstrategia("ponderada")).isEqualTo(resultado);

        InOrder orden = inOrder(strategyRepository);
        orden.verify(strategyRepository).guardarEstado(new EstadoEstrategiaCotizacion("ponderada"));
        orden.verify(strategyRepository).buscarPagina(null, 200);
    }

    /** Una estrategia registrada sin configuración completa no puede activarse. */
    @Test
    void noActivaEstrategiaSinConfiguracion() {
        when(strategyRepository.bloquearEstado())
                .thenReturn(Optional.of(new EstadoEstrategiaCotizacion(null)));
        when(strategyRepository.buscarConfiguracion("ponderada")).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> administrationService.activarEstrategia("ponderada"))
                .isInstanceOf(ConfiguracionIncompletaException.class);

        verify(strategyRepository, never()).guardarEstado(
                new EstadoEstrategiaCotizacion("ponderada"));

        verify(strategyRepository, never()).buscarPagina(null, 200);
    }

    @Test
    void consultarActivaSinConfigurarArrojaExcepcion() {
        NoHayEstrategiaActivaConfiguradaException error = assertThrows(NoHayEstrategiaActivaConfiguradaException.class,
                () -> administrationService.consultarActiva());
        assertEquals(error.getMessage(),"No hay una estrategia activa configurada");
    }

    @Test
    void consultarActivaConEstrategiaActivaNoDisponibleArrojaExcepcion() {
        when(strategyRepository.buscarEstado()).thenReturn(
                Optional.of(new EstadoEstrategiaCotizacion("estrategia inexistente"))
        );
        EstrategiaActivaNoDisponibleException error =
                assertThrows(EstrategiaActivaNoDisponibleException.class,
                        () -> administrationService.consultarActiva());

        assertEquals(error.getMessage(),"La estrategia activa no está disponible");
    }

    @Test
    void consultarActivaDevuelveEstrategiaActiva() {
        when(strategyRepository.buscarEstado()).thenReturn(
                Optional.of(new EstadoEstrategiaCotizacion("ponderada"))
        );
        when(strategyRepository.buscarConfiguracion("ponderada")).thenReturn(
                Optional.of(configuracionPonderada())
        );
        CotizacionService.EstrategiaEstado response = administrationService.consultarActiva();

        assertEquals(response.strategyId(), "ponderada");
        assertTrue(response.active());
    }

    @Test
    void listarEstrategiasRetornaLasEstrategiasPersistidas() {
        List<CotizacionService.EstrategiaEstado> lista = administrationService.listarEstrategias();
        assertTrue(!lista.isEmpty());
        assertEquals(lista.size(), 2);
        assertTrue(lista.stream().anyMatch(str -> Objects.equals(str.strategyId(), "ponderada")));
        assertTrue(lista.stream().anyMatch(str -> Objects.equals(str.strategyId(), "ponderada-con-penalizacion")));
    }

    @Test
    void lanzaExcepcionCuandoNoExisteHistorial() {
        assertThrows(
                HistorialNoEncontradoException.class,
                () -> administrationService.buscarHistorialPorFecha(
                        jugador.getEstadisticasDeportivas().getJugador().getId(), fechaBuscada
                )
        );
    }

    @Test
    void lanzaExcepcionCuandoNoExisteJugador() {
        jugadorRepository.deleteAll();

        assertThrows(JugadorNoEncontradoException.class,
                () -> administrationService.buscarHistorialPorFecha(
                        jugador.getEstadisticasDeportivas().getJugador().getId(), fechaBuscada
                )
        );
    }

    @Test
    void devuelvePosteriorCuandoNoExisteAnterior() {
        guardarHistorial(
                fechaBuscada.plusDays(2).atTime(12, 0)
        );

        UUID id = jugador.getEstadisticasDeportivas().getJugador().getId();

        HistorialCotizacion resultado =
                administrationService.buscarHistorialPorFecha(id, fechaBuscada);

        List<HistorialCotizacion> list =
                administrationService.buscarHistorialById(id);

        assertEquals(list.getFirst().getId(), resultado.getId());
    }

    @Test
    void devuelveAnteriorCuandoNoExistePosterior() {
        guardarHistorial(
                fechaBuscada.minusDays(2).atTime(12, 0)
        );

        UUID id = jugador.getEstadisticasDeportivas().getJugador().getId();

        HistorialCotizacion resultado =
                administrationService.buscarHistorialPorFecha(id, fechaBuscada);

        List<HistorialCotizacion> list =
                administrationService.buscarHistorialById(id);

        assertEquals(list.getFirst().getId(), resultado.getId());
    }

    @Test
    void devuelveAnteriorCuandoEsLaMasCercana() {
        guardarHistorial(
                fechaBuscada.minusDays(1).atTime(12, 0)
        );

        guardarHistorial(
                fechaBuscada.plusDays(3).atTime(12, 0)
        );

        UUID id = jugador.getEstadisticasDeportivas().getJugador().getId();

        HistorialCotizacion resultado =
                administrationService.buscarHistorialPorFecha(id, fechaBuscada);

        List<HistorialCotizacion> list =
                administrationService.buscarHistorialById(id);

        HistorialCotizacion anterior = list.stream()
                .filter(h -> h.getFecha().toLocalDate().equals(fechaBuscada.minusDays(1)))
                .findFirst()
                .orElseThrow();

        assertEquals(anterior.getId(), resultado.getId());
    }

    @Test
    void devuelvePosteriorCuandoEsLaMasCercana() {
        guardarHistorial(
                fechaBuscada.minusDays(4).atTime(12, 0)
        );

        guardarHistorial(
                fechaBuscada.plusDays(1).atTime(12, 0)
        );

        UUID id = jugador.getEstadisticasDeportivas().getJugador().getId();

        HistorialCotizacion resultado =
                administrationService.buscarHistorialPorFecha(id, fechaBuscada);

        List<HistorialCotizacion> list =
                administrationService.buscarHistorialById(id);

        HistorialCotizacion posterior = list.stream()
                .filter(h -> h.getFecha().toLocalDate().equals(fechaBuscada.plusDays(1)))
                .findFirst()
                .orElseThrow();

        assertEquals(posterior.getId(), resultado.getId());
    }

    @Test
    void devuelveAnteriorCuandoAmbasEstanAMismaDistancia() {
        guardarHistorial(
                fechaBuscada.minusDays(1).atTime(23, 59)
        );

        guardarHistorial(
                fechaBuscada.plusDays(1).atTime(0, 1)
        );

        UUID id = jugador.getEstadisticasDeportivas().getJugador().getId();

        HistorialCotizacion resultado =
                administrationService.buscarHistorialPorFecha(id, fechaBuscada);

        List<HistorialCotizacion> list =
                administrationService.buscarHistorialById(id);

        HistorialCotizacion anterior = list.stream()
                .filter(h -> h.getFecha().toLocalDate().equals(fechaBuscada.minusDays(1)))
                .findFirst()
                .orElseThrow();

        assertEquals(anterior.getId(), resultado.getId());
    }

    @Test
    void devuelveCotizacionDelMismoDiaCuandoExiste() {
        guardarHistorial(
                fechaBuscada.minusDays(2).atTime(12, 0)
        );

        guardarHistorial(
                fechaBuscada.atTime(14, 30)
        );

        UUID id = jugador.getEstadisticasDeportivas().getJugador().getId();

        HistorialCotizacion resultado =
                administrationService.buscarHistorialPorFecha(id, fechaBuscada);

        List<HistorialCotizacion> list =
                administrationService.buscarHistorialById(id);

        HistorialCotizacion exacta = list.stream()
                .filter(h -> h.getFecha().toLocalDate().equals(fechaBuscada))
                .findFirst()
                .orElseThrow();

        assertEquals(exacta.getId(), resultado.getId());
    }



    private BolsaDeJugador crearJugador() {
        Jugador tempJugador = new Jugador(
                10, "Messi", "Barza", Liga.LALIGA,
                Posicion.DEFENDER, 33, 150, 79);

        EstadisticasDeportivas estadisticas = new EstadisticasDeportivas(
                tempJugador, false, 10, 10, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, 10000);

        BolsaDeJugador bolsa = new BolsaDeJugador(estadisticas, BigDecimal.ONE,
                LocalDateTime.now(), "fuente", 100);
        return jugadorRepository.save(bolsa);
    }

    private void guardarHistorial(LocalDateTime fecha) {
        HistorialCotizacion historial = new HistorialCotizacion(jugador.getId(), BigDecimal.TEN, fecha);

        historialRepository.guardarHistorial(List.of(historial));
    }

    private ConfiguracionEstrategiaPonderada configuracionPonderada() {
        return new ConfiguracionEstrategiaPonderada(BigDecimal.ONE, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ONE);
    }
}