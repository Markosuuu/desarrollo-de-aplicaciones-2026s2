package com.prontaentrega.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.prontaentrega.models.estrategias.ConfiguracionEstrategiaPonderada;
import com.prontaentrega.services.CotizacionService;
import com.prontaentrega.services.dto.ResultadoRecotizacion;
import com.prontaentrega.utils.AbstractIntegrationTest;
import java.math.BigDecimal;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Comprueba aislamiento de las operaciones administrativas con clave maestra. */
@SpringBootTest
@AutoConfigureMockMvc
@Tag("integracion")
class CotizacionControllerSecurityTest extends AbstractIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CotizacionService administrationService;

    @Value("${api-key.master-secret}")
    private String masterSecret;

    /** Sin master key, las tres operaciones de escritura reciben 401 y no llaman servicios. */
    @Test
    void rechazaEscriturasSinClaveMaestra() throws Exception {
        mockMvc.perform(post("/quotes/recalculate")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/quotes/recalculate").header("X-Master-Key", "incorrecta"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/quotes/strategies/ponderada/config")
                        .contentType(MediaType.APPLICATION_JSON).content(configuracionJson()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/quotes/strategies/ponderada/config")
                        .header("X-Master-Key", "incorrecta")
                        .contentType(MediaType.APPLICATION_JSON).content(configuracionJson()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/quotes/strategy").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"strategyId\":\"ponderada\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/quotes/strategy").header("X-Master-Key", "incorrecta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"strategyId\":\"ponderada\"}"))
                .andExpect(status().isUnauthorized());

        verify(administrationService, never()).recalcularEstrategiaActiva();
        verify(administrationService, never()).activarEstrategia(anyString());
        verify(administrationService, never()).actualizarConfiguracion(anyString(), any());
    }

    /** X-API-Key y JWT no habilitan operaciones protegidas con master key. */
    @Test
    void noAceptaApiKeyNiJwtComoSustitutosDeClaveMaestra() throws Exception {
        mockMvc.perform(post("/quotes/recalculate").header("X-API-Key", "clave-externa"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/quotes/strategy")
                        .header("Authorization", "Bearer token-de-usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"strategyId\":\"ponderada\"}"))
                .andExpect(status().isUnauthorized());
        verify(administrationService, never()).recalcularEstrategiaActiva();
        verify(administrationService, never()).activarEstrategia(anyString());
    }

    /** La clave válida permite recotización, actualización y activación. */
    @Test
    void permiteLasTresEscriturasConClaveMaestra() throws Exception {
        when(administrationService.recalcularEstrategiaActiva())
                .thenReturn(new ResultadoRecotizacion("ponderada", 1, 0));
        when(administrationService.actualizarConfiguracion(anyString(), any()))
                .thenReturn(new ConfiguracionEstrategiaPonderada(BigDecimal.ONE, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ONE));
        when(administrationService.activarEstrategia("ponderada"))
                .thenReturn(new ResultadoRecotizacion("ponderada", 1, 0));

        mockMvc.perform(post("/api/quotes/recalculate").header("X-Master-Key", masterSecret))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/quotes/strategies/ponderada/config")
                        .header("X-Master-Key", masterSecret)
                        .contentType(MediaType.APPLICATION_JSON).content(configuracionJson()))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/quotes/strategy").header("X-Master-Key", masterSecret)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"strategyId\":\"ponderada\"}"))
                .andExpect(status().isOk());
    }

    private String configuracionJson() {
        return """
                {"pesoGoles":1,"pesoAsistencias":0,"pesoDisparosPorPartido":0,
                 "pesoPasesClavePorPartido":0,"pesoRegatesGanadosPorPartido":0,
                 "pesoFaltasCometidasPorPartido":0,"pesoRating":0,"factorEscala":1}
                """;
    }
}