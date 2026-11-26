package com.prontaentrega.persistence.dto.mercado;

import java.util.UUID;

public interface JugadorResumenInventarioProjection {

    UUID getJugadorId();

    String getJugadorNombre();

    Long getCantidadTokens();
}
