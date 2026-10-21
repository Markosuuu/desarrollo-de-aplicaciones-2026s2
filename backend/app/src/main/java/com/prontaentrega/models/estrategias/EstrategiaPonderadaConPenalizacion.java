package com.prontaentrega.models.estrategias;

import com.prontaentrega.models.ConfiguracionEstrategiaCotizacion;
import com.prontaentrega.models.ConfiguracionEstrategiaPonderadaPenalizacion;
import com.prontaentrega.models.DatosValoracion;
import com.prontaentrega.models.exception.ConfiguracionNoCorrespondiente;
import com.prontaentrega.models.exception.CotizacionInvalidaException;
import com.prontaentrega.models.exception.DatosValoracionInvalidaException;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

/** Calcula la cotización ponderada y aplica la penalización por regates bajo umbral. */
@Component
public class EstrategiaPonderadaConPenalizacion implements EstrategiaCotizacion {
    /** Devuelve el identificador persistido de la estrategia. */
    @Override
    public String estrategiaId() {
        return "ponderada-con-penalizacion";
    }

    /** Devuelve el nombre público en español. */
    @Override
    public String nombreEstrategia() {
        return "Métricas ponderadas con penalización";
    }

    /** Declara la configuración requerida y mantiene verificable la asociación del algoritmo. */
    @Override
    public Class<ConfiguracionEstrategiaPonderadaPenalizacion> tipoConfiguracion() {
        return ConfiguracionEstrategiaPonderadaPenalizacion.class;
    }

    /** Aplica la fórmula ponderada penalizada y valida el resultado de mercado. */
    @Override
    public BigDecimal calcular(DatosValoracion datos,
                               ConfiguracionEstrategiaCotizacion configuracion) {
        if (!(configuracion instanceof ConfiguracionEstrategiaPonderadaPenalizacion pesos)) {
            throw new ConfiguracionNoCorrespondiente(
                    "La configuración no corresponde a la estrategia ponderada con penalización");
        }
        validar(datos);

        BigDecimal score = pesos.pesoGoles().multiply(BigDecimal.valueOf(datos.goles()))
                .add(pesos.pesoAsistencias().multiply(BigDecimal.valueOf(datos.asistencias())))
                .add(pesos.pesoDisparosPorPartido().multiply(datos.disparosPorPartido()))
                .add(pesos.pesoRating().multiply(datos.rating()))
                .subtract(pesos.pesoFaltasCometidasPorPartido()
                        .multiply(datos.faltasCometidasPorPartido()));

        if (datos.dribblesGanadosPorPartido()
                .compareTo(pesos.umbralRegatesGanadosPorPartido()) < 0) {
            score = score.subtract(pesos.factorEscala());
        }

        BigDecimal cotizacion = datos.cotizacionVigente().add(score);
        if (cotizacion.compareTo(BigDecimal.ONE) <= 0) {
            throw new CotizacionInvalidaException("La cotización calculada debe ser mayor que 1");
        }
        return cotizacion;
    }

    /** Rechaza datos requeridos nulos o negativos para que el servicio omita esta bolsa. */
    private void validar(DatosValoracion datos) {
        if (datos == null || datos.cotizacionVigente() == null || datos.goles() == null
                || datos.asistencias() == null || datos.disparosPorPartido() == null
                || datos.dribblesGanadosPorPartido() == null
                || datos.faltasCometidasPorPartido() == null || datos.rating() == null) {
            throw new DatosValoracionInvalidaException("Faltan métricas requeridas para valorar al jugador");
        }
        if (datos.goles() < 0 || datos.asistencias() < 0
                || List.of(datos.disparosPorPartido(), datos.dribblesGanadosPorPartido(),
                datos.faltasCometidasPorPartido(), datos.rating()).stream()
                .anyMatch(valor -> valor.compareTo(BigDecimal.ZERO) < 0)) {
            throw new DatosValoracionInvalidaException("Las métricas del jugador contienen valores inválidos");
        }
    }
}