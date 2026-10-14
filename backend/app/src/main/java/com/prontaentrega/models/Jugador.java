package com.prontaentrega.models;

import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Jugador {
    private UUID id;
    private Integer whoscoredId;
    private String nombre;
    private String equipo;
    private Liga liga;
    private Posicion posicion;
    private Integer edad;
    private Integer altura;
    private Integer peso;

    public Jugador(
            Integer whoscoredId,
            String nombre,
            String equipo,
            Liga liga,
            Posicion posicion,
            Integer edad,
            Integer altura,
            Integer peso) {
        if (whoscoredId == null) {
            // TODO: Cambiar a una excepción personalizada
            throw new IllegalArgumentException("El ID de WhoScored es obligatorio");
        }
        this.whoscoredId = whoscoredId;
        this.nombre = validarTexto(nombre, "El nombre es obligatorio");
        this.equipo = validarTexto(equipo, "El equipo es obligatorio");
        if (liga == null) {
            throw new IllegalArgumentException("La liga es obligatoria");
        }
        this.liga = liga;
        this.posicion = posicion;
        validarNoNegativo(edad, "La edad no puede ser negativa");
        validarNoNegativo(altura, "La altura no puede ser negativa");
        validarNoNegativo(peso, "El peso no puede ser negativo");
        this.edad = edad;
        this.altura = altura;
        this.peso = peso;
    }

    private static String validarTexto(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private static void validarNoNegativo(Integer value, String message) {
        if (value != null && value < 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
