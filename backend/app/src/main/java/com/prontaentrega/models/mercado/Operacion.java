package com.prontaentrega.models.mercado;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import com.prontaentrega.models.exception.ValorNegativoException;
import com.prontaentrega.models.jugadores.Jugador;
import com.prontaentrega.models.auth.Usuario;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class Operacion {
    private UUID id;
    private final Usuario usuario;
    private final Usuario contraparte;
    private final Jugador jugador;
    private final Integer cantidad;
    private final BigDecimal precioPorToken;
    private final BigDecimal total;
    private final TipoOperacion tipo;
    private final EstadoOperacion estado;
    private final LocalDateTime creadaEn;

    public Operacion(
            @NonNull Usuario usuario,
            @NonNull Usuario contraparte,
            @NonNull Jugador jugador,
            @NonNull Integer cantidad,
            @NonNull BigDecimal precioPorToken,
            @NonNull BigDecimal total,
            @NonNull TipoOperacion tipo,
            @NonNull EstadoOperacion estado) {

        if (cantidad <= 0) {
            throw new ValorNegativoException("La cantidad debe ser mayor que 0");
        }

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValorNegativoException("El total no puede ser negativo");
        }

        this.usuario = usuario;
        this.contraparte = contraparte;
        this.jugador = jugador;
        this.cantidad = cantidad;
        this.precioPorToken = precioPorToken;
        this.total = total;
        this.tipo = tipo;
        this.estado = estado;
        this.creadaEn = LocalDateTime.now(ZoneId.systemDefault());
    }
}
