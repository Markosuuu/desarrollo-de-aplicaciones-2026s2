package com.prontaentrega.persistence.dto.mercado;

import java.math.BigDecimal;
import java.util.UUID;

public interface JugadorResumenValorInventarioProjection {

    UUID getJugadorId();

    String getJugadorNombre();

    Long getCantidad();

    BigDecimal getValor();
}