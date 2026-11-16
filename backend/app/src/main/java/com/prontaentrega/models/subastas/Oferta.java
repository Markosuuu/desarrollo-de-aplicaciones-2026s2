package com.prontaentrega.models.subastas;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import com.prontaentrega.models.auth.Usuario;
import com.prontaentrega.models.exception.CampoFaltanteException;
import com.prontaentrega.models.exception.ValorNegativoException;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class Oferta {
    private UUID id;
    private final Subasta subasta;
    private final Usuario usuario;
    private final BigDecimal puja;
    private final LocalDateTime creadoEn;
    private Boolean esSuperior;
    private final String idempotencyKey;

    public Oferta(
            @NonNull Subasta subasta,
            @NonNull Usuario usuario,
            @NonNull BigDecimal puja,
            @NonNull String idempotencyKey) {
        this(subasta, usuario, puja, LocalDateTime.now(ZoneId.systemDefault()), idempotencyKey);
    }

    public Oferta(
            @NonNull Subasta subasta,
            @NonNull Usuario usuario,
            @NonNull BigDecimal puja,
            @NonNull LocalDateTime creadoEn,
            @NonNull String idempotencyKey) {

        if (puja.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValorNegativoException("La puja debe ser mayor que 0");
        }

        if (idempotencyKey.isBlank()) {
            throw new CampoFaltanteException("La clave de idempotencia es obligatoria");
        }

        this.subasta = subasta;
        this.usuario = usuario;
        this.puja = puja;
        this.esSuperior = true;
        this.creadoEn = creadoEn;
        this.idempotencyKey = idempotencyKey;
    }

    public UUID getSubastaId() {
        return subasta.getId();
    }

    public UUID getUsuarioId() {
        return usuario.getId();
    }

}
