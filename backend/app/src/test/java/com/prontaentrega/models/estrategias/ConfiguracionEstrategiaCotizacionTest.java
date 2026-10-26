package com.prontaentrega.models.estrategias;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.prontaentrega.models.ConfiguracionEstrategiaCotizacion;
import com.prontaentrega.controllers.dtos.cotizacion.CotizacionStrategyConfigRequest;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** Verifica que la identidad se mantenga en Strategy y la configuración quede asociada por tipo. */
class ConfiguracionEstrategiaCotizacionTest {
    /** Las configuraciones no duplican el ID estable que posee cada estrategia. */
    @Test
    void configuracionNoDeclaraIdentificadorDuplicado() {
        assertThat(ConfiguracionEstrategiaCotizacion.class.getDeclaredMethods())
                .extracting(Method::getName)
                .doesNotContain("estrategiaId");
    }

    /** Cada estrategia declara unívocamente la configuración tipada que puede consumir. */
    @Test
    void estrategiaDeclaraSuTipoDeConfiguracion() {
        EstrategiaPonderada ponderada = new EstrategiaPonderada();
        EstrategiaPonderadaConPenalizacion penalizada =
                new EstrategiaPonderadaConPenalizacion();

        assertThat(ponderada.estrategiaId()).isEqualTo("ponderada");
        assertThat(ponderada.tipoConfiguracion()).isEqualTo(ConfiguracionEstrategiaPonderada.class);
        assertThat(penalizada.estrategiaId()).isEqualTo("ponderada-con-penalizacion");
        assertThat(penalizada.tipoConfiguracion())
                .isEqualTo(ConfiguracionEstrategiaPonderadaPenalizacion.class);
    }

    /** Las reglas actuales de ambas configuraciones siguen aplicándose tras quitar su ID. */
    @Test
    void lasConfiguracionesMantienenSusValidaciones() {
        assertThat(new ConfiguracionEstrategiaPonderada(BigDecimal.ONE, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ONE).pesoGoles()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(new ConfiguracionEstrategiaPonderadaPenalizacion(BigDecimal.ONE,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ONE).umbralRegatesGanadosPorPartido())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

        /** Valida la forma exacta del request ponderado y su suma unitaria de coeficientes. */
        @Test
        void validaRequestDeEstrategiaPonderada() {
                CotizacionStrategyConfigRequest request = new CotizacionStrategyConfigRequest();
                request.setPesoGoles(BigDecimal.ONE);
                request.setPesoAsistencias(BigDecimal.ZERO);
                request.setPesoDisparosPorPartido(BigDecimal.ZERO);
                request.setPesoPasesClavePorPartido(BigDecimal.ZERO);
                request.setPesoRegatesGanadosPorPartido(BigDecimal.ZERO);
                request.setPesoFaltasCometidasPorPartido(BigDecimal.ZERO);
                request.setPesoRating(BigDecimal.ZERO);
                request.setFactorEscala(BigDecimal.ONE.negate());

                assertThat(request.isConfiguracionValida()).isTrue();
                assertThat(request.aConfiguracion()).isInstanceOf(ConfiguracionEstrategiaPonderada.class);

                request.setPesoGoles(new BigDecimal("0.5"));
                assertThat(request.isConfiguracionValida()).isFalse();
        }

        /** Valida que el umbral penalizado no se confunda con un coeficiente de la suma. */
        @Test
        void validaRequestPenalizadoConUmbralSeparado() {
                CotizacionStrategyConfigRequest request = new CotizacionStrategyConfigRequest();
                request.setPesoGoles(BigDecimal.ONE);
                request.setPesoAsistencias(BigDecimal.ZERO);
                request.setPesoDisparosPorPartido(BigDecimal.ZERO);
                request.setPesoRating(BigDecimal.ZERO);
                request.setPesoFaltasCometidasPorPartido(BigDecimal.ZERO);
                request.setUmbralRegatesGanadosPorPartido(BigDecimal.ZERO);
                request.setFactorEscala(BigDecimal.ZERO);

                assertThat(request.isConfiguracionValida()).isTrue();
                assertThat(request.aConfiguracion())
                                .isInstanceOf(ConfiguracionEstrategiaPonderadaPenalizacion.class);

                request.setPesoRegatesGanadosPorPartido(BigDecimal.ZERO);
                assertThat(request.isConfiguracionValida()).isFalse();
        }

        /** Rechaza pesos negativos, sumas incorrectas y cualquier campo JSON no reconocido. */
        @Test
        void rechazaPesosInvalidosYCamposDesconocidosEnRequest() {
                CotizacionStrategyConfigRequest request = new CotizacionStrategyConfigRequest();
                request.setPesoGoles(new BigDecimal("0.5"));
                request.setPesoAsistencias(new BigDecimal("0.5"));
                request.setPesoDisparosPorPartido(BigDecimal.ZERO);
                request.setPesoPasesClavePorPartido(BigDecimal.ZERO);
                request.setPesoRegatesGanadosPorPartido(BigDecimal.ZERO);
                request.setPesoFaltasCometidasPorPartido(BigDecimal.ZERO);
                request.setPesoRating(BigDecimal.ZERO);
                request.setFactorEscala(BigDecimal.ONE);
                request.agregarCampoDesconocido("pesoExtra", BigDecimal.ONE);

                assertThat(request.isConfiguracionValida()).isFalse();
        }

        /** Rechaza precisión que la columna NUMERIC(18,8) alteraría al persistir. */
        @Test
        void rechazaFactorEscalaQueRequiereRedondeoDeBaseDeDatos() {
                CotizacionStrategyConfigRequest request = new CotizacionStrategyConfigRequest();
                request.setPesoGoles(BigDecimal.ONE);
                request.setPesoAsistencias(BigDecimal.ZERO);
                request.setPesoDisparosPorPartido(BigDecimal.ZERO);
                request.setPesoPasesClavePorPartido(BigDecimal.ZERO);
                request.setPesoRegatesGanadosPorPartido(BigDecimal.ZERO);
                request.setPesoFaltasCometidasPorPartido(BigDecimal.ZERO);
                request.setPesoRating(BigDecimal.ZERO);
                request.setFactorEscala(new BigDecimal("0.123456789"));

                assertThat(request.isConfiguracionValida()).isFalse();
        }

    /** Rechaza coeficientes negativos o cuya suma difiere de uno. */
    @Test
    void rechazaConfiguracionPonderadaInvalida() {
        assertThatThrownBy(() -> configuracionPonderada(
                "-0.1", "0.2", "0.2", "0.2", "0.2", "0.2", "0.1", "1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /** Construye configuración válida o inválida de la estrategia ponderada para los tests. */
    private ConfiguracionEstrategiaPonderada configuracionPonderada(
            String goles, String asistencias, String disparos, String pases,
            String regates, String faltas, String rating, String escala) {
        return new ConfiguracionEstrategiaPonderada(new BigDecimal(goles),
                new BigDecimal(asistencias), new BigDecimal(disparos), new BigDecimal(pases),
                new BigDecimal(regates), new BigDecimal(faltas), new BigDecimal(rating),
                new BigDecimal(escala));
    }
}