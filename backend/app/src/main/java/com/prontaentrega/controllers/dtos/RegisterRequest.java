package com.prontaentrega.controllers.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "VALIDACION_INVALIDA") String nombre,
        @NotBlank(message = "VALIDACION_INVALIDA") @Email(message = "VALIDACION_INVALIDA") String correo,
        @NotBlank(message = "VALIDACION_INVALIDA") @Size(min = 8, message = "VALIDACION_INVALIDA") String password
) {
}
