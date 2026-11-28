package com.prontaentrega.services.exceptions.cotizacion;

/** Indica que la configuración recibida incumple una regla de entrada quote. */
public class ConfiguracionNoValidaParaEstrategiaException extends RuntimeException {
    /** Crea un error de validación con mensaje seguro para el cliente. */
    public ConfiguracionNoValidaParaEstrategiaException(String message) {
        super(message);
    }
}