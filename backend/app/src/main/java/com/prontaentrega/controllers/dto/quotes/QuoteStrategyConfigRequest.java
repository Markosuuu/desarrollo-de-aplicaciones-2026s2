package com.prontaentrega.controllers.dto.quotes;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.prontaentrega.models.ConfiguracionEstrategiaCotizacion;
import com.prontaentrega.models.ConfiguracionEstrategiaPonderada;
import com.prontaentrega.models.ConfiguracionEstrategiaPonderadaPenalizacion;
import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/** Body validado de configuración para una de las estrategias quote disponibles. */
@Getter
@Setter
public class QuoteStrategyConfigRequest {
    private BigDecimal pesoGoles;
    private BigDecimal pesoAsistencias;
    private BigDecimal pesoDisparosPorPartido;
    private BigDecimal pesoPasesClavePorPartido;
    private BigDecimal pesoRegatesGanadosPorPartido;
    private BigDecimal pesoFaltasCometidasPorPartido;
    private BigDecimal pesoRating;
    private BigDecimal umbralRegatesGanadosPorPartido;
    private BigDecimal factorEscala;
    private final Map<String, Object> camposDesconocidos = new HashMap<>();

    /** Captura campos JSON fuera del contrato para rechazarlos en la validación del body. */
    @JsonAnySetter
    public void agregarCampoDesconocido(String nombre, Object valor) {
        camposDesconocidos.put(nombre, valor);
    }

    /** Exige exactamente la forma de un DTO de estrategia, pesos no negativos y suma unitaria. */
    @AssertTrue(message = "La configuración debe tener campos válidos y pesos no negativos que sumen 1")
    public boolean isConfiguracionValida() {
        if (!camposDesconocidos.isEmpty() || !precisionPersistible(factorEscala)) {
            return false;
        }
        boolean camposComunesValidos = noNegativo(pesoGoles)
                && noNegativo(pesoAsistencias)
                && noNegativo(pesoDisparosPorPartido)
                && noNegativo(pesoRating)
                && noNegativo(pesoFaltasCometidasPorPartido);
        if (!camposComunesValidos) {
            return false;
        }

        boolean formaPonderada = pesoPasesClavePorPartido != null
                && pesoRegatesGanadosPorPartido != null
                && umbralRegatesGanadosPorPartido == null
                && noNegativo(pesoPasesClavePorPartido)
                && noNegativo(pesoRegatesGanadosPorPartido)
                && todosPersistibles(pesoGoles, pesoAsistencias, pesoDisparosPorPartido,
                pesoPasesClavePorPartido, pesoRegatesGanadosPorPartido,
                pesoFaltasCometidasPorPartido, pesoRating)
                && suma(pesoGoles, pesoAsistencias, pesoDisparosPorPartido,
                pesoPasesClavePorPartido, pesoRegatesGanadosPorPartido,
                pesoFaltasCometidasPorPartido, pesoRating).compareTo(BigDecimal.ONE) == 0;

        boolean formaPenalizada = pesoPasesClavePorPartido == null
                && pesoRegatesGanadosPorPartido == null
                && umbralRegatesGanadosPorPartido != null
                && precisionPersistible(umbralRegatesGanadosPorPartido)
                && todosPersistibles(pesoGoles, pesoAsistencias, pesoDisparosPorPartido,
                pesoRating, pesoFaltasCometidasPorPartido)
                && suma(pesoGoles, pesoAsistencias, pesoDisparosPorPartido,
                pesoRating, pesoFaltasCometidasPorPartido).compareTo(BigDecimal.ONE) == 0;

        return formaPonderada || formaPenalizada;
    }

    /** Convierte la forma validada del request a su configuración de dominio correspondiente. */
    public ConfiguracionEstrategiaCotizacion aConfiguracion() {
        if (umbralRegatesGanadosPorPartido != null) {
            return new ConfiguracionEstrategiaPonderadaPenalizacion(pesoGoles, pesoAsistencias,
                    pesoDisparosPorPartido, pesoRating, pesoFaltasCometidasPorPartido,
                    umbralRegatesGanadosPorPartido, factorEscala);
        }
        return new ConfiguracionEstrategiaPonderada(pesoGoles, pesoAsistencias,
                pesoDisparosPorPartido, pesoPasesClavePorPartido,
                pesoRegatesGanadosPorPartido, pesoFaltasCometidasPorPartido,
                pesoRating, factorEscala);
    }

    /** Indica si un peso está presente y no es negativo. */
    private boolean noNegativo(BigDecimal valor) {
        return valor != null && valor.compareTo(BigDecimal.ZERO) >= 0;
    }

    /** Comprueba compatibilidad exacta con NUMERIC(18,8), evitando redondeo al persistir. */
    private boolean precisionPersistible(BigDecimal valor) {
        return valor != null && valor.scale() <= 8 && valor.precision() - valor.scale() <= 10;
    }

    /** Valida precisión de todos los coeficientes persistidos por el mismo esquema decimal. */
    private boolean todosPersistibles(BigDecimal... valores) {
        for (BigDecimal valor : valores) {
            if (!precisionPersistible(valor)) {
                return false;
            }
        }
        return true;
    }

    /** Suma pesos de una fórmula para validar que su total sea unitario. */
    private BigDecimal suma(BigDecimal... valores) {
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal valor : valores) {
            total = total.add(valor);
        }
        return total;
    }

}