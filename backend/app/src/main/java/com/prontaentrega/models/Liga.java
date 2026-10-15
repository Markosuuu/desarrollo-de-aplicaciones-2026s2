package com.prontaentrega.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum Liga {
    BUNDESLIGA("Bundesliga"),
    SERIE_A("Serie A"),
    LIGUE_1("Ligue 1"),
    LALIGA("LaLiga"),
    PREMIER_LEAGUE("Premier League");

    private final String value;

    Liga(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static Liga fromValue(String value) {
        if (value != null) {
            return Arrays.stream(values())
                    .filter(liga -> liga.value.equalsIgnoreCase(value.trim()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Liga no soportada: " + value));
        }
        throw new IllegalArgumentException("La liga es obligatoria");
    }
}
