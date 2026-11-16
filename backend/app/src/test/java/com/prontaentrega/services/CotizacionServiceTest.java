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
import com.prontaentrega.models.cotizacion.estrategias.ConfiguracionEstrategiaPonderada;
import com.prontaentrega.models.cotizacion.EstadoEstrategiaCotizacion;
import com.prontaentrega.models.cotizacion.estrategias.ConfiguracionEstrategiaPonderadaPenalizacion;
import com.prontaentrega.models.cotizacion.estrategias.EstrategiaPonderada;
import com.prontaentrega.models.cotizacion.estrategias.EstrategiaPonderadaConPenalizacion;
import com.prontaentrega.persistence.repository.CotizacionRepository;
import com.prontaentrega.persistence.repository.HistorialCotizacionRepository;
import com.prontaentrega.services.dto.ResultadoRecotizacion;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.prontaentrega.services.exceptions.cotizacion.ConfiguracionIncompletaException;
import com.prontaentrega.services.exceptions.cotizacion.EstrategiaActivaNoDisponibleException;
import com.prontaentrega.services.exceptions.cotizacion.NoHayEstrategiaActivaConfiguradaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Verifica la coordinación administrativa sin cargar base de datos. */
@ExtendWith(MockitoExtension.class)
@Tag("integracion")
class CotizacionServiceTest {
    @Mock
    private CotizacionRepository strategyRepository;
    private HistorialCotizacionRepository historialRepository;
    private CotizacionService administrationService;

    /** Registra las dos estrategias y valida su asociación tipada con los providers. */
    @BeforeEach
    void crearServicio() {
        when(strategyRepository.estrategiaIdsConfigurables())
                .thenReturn(java.util.Set.of("ponderada", "ponderada-con-penalizacion"));
        doReturn(ConfiguracionEstrategiaPonderada.class)
                .when(strategyRepository).tipoConfiguracion("ponderada");
        doReturn(ConfiguracionEstrategiaPonderadaPenalizacion.class)
                .when(strategyRepository).tipoConfiguracion("ponderada-con-penalizacion");
        when(strategyRepository.estrategiaIdsConfigurables())
                .thenReturn(java.util.Set.of("ponderada", "ponderada-con-penalizacion"));
        administrationService = new CotizacionService(strategyRepository, historialRepository,
                List.of(new EstrategiaPonderada(), new EstrategiaPonderadaConPenalizacion()));
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

    private ConfiguracionEstrategiaPonderada configuracionPonderada() {
        return new ConfiguracionEstrategiaPonderada(BigDecimal.ONE, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ONE);
    }
}