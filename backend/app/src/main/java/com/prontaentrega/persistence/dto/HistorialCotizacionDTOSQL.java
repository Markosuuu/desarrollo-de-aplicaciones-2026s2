package com.prontaentrega.persistence.dto;

import com.prontaentrega.models.HistorialCotizacion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "HistorialCotizacion")
@Table(name = "historial_cotizaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistorialCotizacionDTOSQL {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bolsa_de_jugador_id", nullable = false)
    private BolsaDeJugadorDTOSQL bolsaDeJugador;

    @Column(nullable = false)
    private BigDecimal precio;

    @Column(nullable = false)
    private LocalDateTime fecha;

    public static HistorialCotizacionDTOSQL desdeModelo(HistorialCotizacion registro, BolsaDeJugadorDTOSQL bolsa) {
        return new HistorialCotizacionDTOSQL(registro.getId(),
                                            bolsa,
                                            registro.getPrecio(),
                                            registro.getFecha());
    }

    public HistorialCotizacion aModelo() {
        HistorialCotizacion historial = new HistorialCotizacion(id, bolsaDeJugador.aModelo().getId(), precio, fecha);
        return historial;
    }
}
