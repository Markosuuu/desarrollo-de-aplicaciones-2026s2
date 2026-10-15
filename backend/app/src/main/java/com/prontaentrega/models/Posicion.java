package com.prontaentrega.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum Posicion {
    DEFENDER("Defender"),
    FORWARD("Forward"),
    MIDFIELDER("Midfielder"),
    GOALKEEPER("Goalkeeper");

    private final String value;

    Posicion(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static Posicion fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(posicion -> posicion.value.equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Posicion no soportada: " + value));
    }
}
