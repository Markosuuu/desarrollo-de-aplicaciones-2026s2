package com.prontaentrega.persistence.dto;

import com.prontaentrega.models.Jugador;
import com.prontaentrega.models.Liga;
import com.prontaentrega.models.Posicion;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "Jugador")
@Table(name = "jugadores")
@Getter
@Setter
@NoArgsConstructor
public class JugadorDTOSQL {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "whoscored_id", nullable = false, unique = true)
    private Integer whoscoredId;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String equipo;

    @Column(nullable = false)
    @Convert(converter = LigaAttributeConverter.class)
    private Liga liga;

    @Column
    @Convert(converter = PosicionAttributeConverter.class)
    private Posicion posicion;

    @Column(nullable = false)
    private Integer edad;

    @Column
    private Integer altura;

    @Column
    private Integer peso;

    static JugadorDTOSQL desdeJugador(Jugador jugador) {
        JugadorDTOSQL dto = new JugadorDTOSQL();
        dto.id = jugador.getId();
        dto.whoscoredId = jugador.getWhoscoredId();
        dto.nombre = jugador.getNombre();
        dto.equipo = jugador.getEquipo();
        dto.liga = jugador.getLiga();
        dto.posicion = jugador.getPosicion();
        dto.edad = jugador.getEdad();
        dto.altura = jugador.getAltura();
        dto.peso = jugador.getPeso();
        return dto;
    }

    Jugador aJugador() {
        Jugador jugador = new Jugador();
        jugador.setId(id);
        jugador.setWhoscoredId(whoscoredId);
        jugador.setNombre(nombre);
        jugador.setEquipo(equipo);
        jugador.setLiga(liga);
        jugador.setPosicion(posicion);
        jugador.setEdad(edad);
        jugador.setAltura(altura);
        jugador.setPeso(peso);
        return jugador;
    }
}
