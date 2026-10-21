package com.prontaentrega.services.dto;

/** Resultado de una ejecución exitosa sobre las bolsas elegibles y omitidas. */
public record ResultadoRecotizacion(
        String strategyId,
        int processedPlayers,
        int skippedPlayers) {
}