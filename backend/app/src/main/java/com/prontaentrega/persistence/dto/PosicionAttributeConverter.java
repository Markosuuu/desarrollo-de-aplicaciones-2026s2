package com.prontaentrega.persistence.dto;

import com.prontaentrega.models.Posicion;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class PosicionAttributeConverter implements AttributeConverter<Posicion, String> {
    @Override
    public String convertToDatabaseColumn(Posicion posicion) {
        return posicion == null ? null : posicion.value();
    }

    @Override
    public Posicion convertToEntityAttribute(String value) {
        return Posicion.fromValue(value);
    }
}
