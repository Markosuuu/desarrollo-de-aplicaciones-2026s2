package com.prontaentrega.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

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

    public Oferta(Subasta subasta, Usuario usuario, BigDecimal puja, String idempotencyKey) {
        this.subasta = subasta;
        this.usuario = usuario;
        this.puja = puja;
        this.esSuperior = true;
        this.creadoEn = LocalDateTime.now(ZoneId.systemDefault());
        this.idempotencyKey = idempotencyKey;
    }

    public Oferta(
            Subasta subasta,
            Usuario usuario,
            BigDecimal puja,
            LocalDateTime creadoEn,
            String idempotencyKey) {
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
