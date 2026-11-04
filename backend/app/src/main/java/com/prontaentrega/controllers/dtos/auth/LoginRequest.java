package com.prontaentrega.controllers.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "VALIDACION_INVALIDA") @Email(message = "VALIDACION_INVALIDA") String correo,
        @NotBlank(message = "VALIDACION_INVALIDA") String password
) {
}
