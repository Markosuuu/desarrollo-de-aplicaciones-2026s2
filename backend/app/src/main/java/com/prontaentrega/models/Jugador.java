package com.prontaentrega.models;

import java.util.UUID;

import com.prontaentrega.models.exception.CampoFaltanteException;
import com.prontaentrega.models.exception.ValorNegativoException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Clase que representa un jugador
 * Contiene la información más básica de un jugador de fútbol
 * <br><br>
 * ... y el id de whoscored...
 */
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

    /**
     * Constructor de la clase Jugador
     * @param whoscoredId Id de WhoScored
     * @param nombre Nombre del jugador
     * @param equipo Equipo al que pertenece el jugador
     * @param liga Liga a la que pertenece el jugador
     * @param posicion Posición que juega
     * @param edad Edad del jugador
     * @param altura Altura del jugador
     * @param peso Peso del jugador
     */
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
            throw new CampoFaltanteException("El ID de WhoScored es obligatorio");
        }
        this.whoscoredId = whoscoredId;
        this.nombre = validarTexto(nombre, "El nombre es obligatorio");
        this.equipo = validarTexto(equipo, "El equipo es obligatorio");
        if (liga == null) {
            throw new CampoFaltanteException("La liga es obligatoria");
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
            throw new CampoFaltanteException(message);
        }
        return value.trim();
    }

    private static void validarNoNegativo(Integer value, String message) {
        if (value != null && value < 0) {
            throw new ValorNegativoException(message);
        }
    }

    public void actualizar(Integer edad, Integer altura, Integer peso, Posicion posicion) {
        if (edad != null) {
            this.edad = edad;
        }
        if (altura != null) {
            this.altura = altura;
        }
        if (peso != null) {
            this.peso = peso;
        }
        if (posicion != null) {
            this.posicion = posicion;
        }
    }
}
