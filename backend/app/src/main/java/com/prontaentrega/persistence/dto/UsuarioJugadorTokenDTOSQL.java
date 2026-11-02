package com.prontaentrega.persistence.dto;

import com.prontaentrega.models.UsuarioJugadorToken;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "UsuarioJugadorToken")
@Table(name = "usuarios_jugadores_tokens")
@Getter
@Setter
@NoArgsConstructor
public class UsuarioJugadorTokenDTOSQL {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "jugador_id", nullable = false)
    private UUID jugadorId;

    @Column(name = "tokens_disponibles", nullable = false)
    private Integer tokensDisponibles;

    @Column(name = "tokens_en_venta", nullable = false)
    private Integer tokensEnVenta;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    public static UsuarioJugadorTokenDTOSQL desdeModelo(UsuarioJugadorToken token) {
        UsuarioJugadorTokenDTOSQL dto = new UsuarioJugadorTokenDTOSQL();
        dto.id = token.getId();
        dto.usuarioId = token.getUsuarioId();
        dto.jugadorId = token.getJugadorId();
        dto.tokensDisponibles = token.getTokensDisponibles();
        dto.tokensEnVenta = token.getTokensEnVenta();
        dto.actualizadoEn = token.getActualizadoEn();
        return dto;
    }

    public UsuarioJugadorToken aModelo() {
        UsuarioJugadorToken token = new UsuarioJugadorToken();
        token.setId(id);
        token.setUsuarioId(usuarioId);
        token.setJugadorId(jugadorId);
        token.setTokensDisponibles(tokensDisponibles);
        token.setTokensEnVenta(tokensEnVenta);
        token.setActualizadoEn(actualizadoEn);
        return token;
    }
}
