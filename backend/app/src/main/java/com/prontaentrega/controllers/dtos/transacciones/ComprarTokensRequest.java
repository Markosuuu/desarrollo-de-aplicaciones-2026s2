package com.prontaentrega.controllers.dtos.transacciones;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ComprarTokensRequest(
        @NotNull UUID anuncioId,
        @NotNull @Positive Integer cantidad) {
}
