package com.prontaentrega.controllers.dtos.subastas;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CrearSubastaRequest(
	@NotNull UUID jugadorId,
	@NotNull @Min(1) Integer cantidad,
	@NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal precioInicial,
	@NotNull @Future LocalDateTime vencimiento) {
}
