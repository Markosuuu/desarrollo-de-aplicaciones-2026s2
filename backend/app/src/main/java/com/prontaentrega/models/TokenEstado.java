package com.prontaentrega.models;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Clase que representa el estado de un token en el sistema.
 */
@Getter
@Setter
@NoArgsConstructor
public class TokenEstado {
    private UUID id;

    private UUID usuarioId;

    private UUID jtiVigente;

    private Integer versionToken;

    private LocalDateTime emitidaEn;

    private LocalDateTime expiraEn;
}
