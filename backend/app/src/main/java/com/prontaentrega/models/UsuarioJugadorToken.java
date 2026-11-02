package com.prontaentrega.models;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Rarete
 */

@Getter
@Setter
@NoArgsConstructor
public class UsuarioJugadorToken {
    private UUID id;
    private UUID usuarioId;
    private UUID jugadorId;
    private Integer tokensDisponibles;
    private Integer tokensEnVenta;
    private LocalDateTime actualizadoEn;

    public UsuarioJugadorToken(UUID usuarioId, UUID jugadorId, Integer tokensDisponibles, Integer tokensEnVenta) {
        this.usuarioId = usuarioId;
        this.jugadorId = jugadorId;
        this.tokensDisponibles = tokensDisponibles;
        this.tokensEnVenta = tokensEnVenta;
        this.actualizadoEn = LocalDateTime.now();
    }
}
