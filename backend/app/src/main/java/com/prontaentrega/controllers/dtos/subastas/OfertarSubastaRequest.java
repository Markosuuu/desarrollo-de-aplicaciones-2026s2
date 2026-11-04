package com.prontaentrega.controllers.dtos.subastas;

import java.math.BigDecimal;
import java.util.UUID;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OfertarSubastaRequest(
	@NotNull UUID subastaId,
	@NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal puja,
	@NotBlank @Size(max = 255) String idempotencyKey) {
}
