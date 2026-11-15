package com.prontaentrega.models.estrategias;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.prontaentrega.models.cotizacion.ConfiguracionEstrategiaCotizacion;
import java.lang.reflect.Method;
import java.math.BigDecimal;

import com.prontaentrega.models.cotizacion.estrategias.ConfiguracionEstrategiaPonderada;
import com.prontaentrega.models.cotizacion.estrategias.ConfiguracionEstrategiaPonderadaPenalizacion;
import com.prontaentrega.models.cotizacion.estrategias.EstrategiaPonderada;
import com.prontaentrega.models.cotizacion.estrategias.EstrategiaPonderadaConPenalizacion;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Verifica que la identidad se mantenga en Strategy y la configuración quede asociada por tipo. */
@Tag("modelo")
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