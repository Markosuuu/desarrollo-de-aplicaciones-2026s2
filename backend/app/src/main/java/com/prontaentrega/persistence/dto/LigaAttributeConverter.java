package com.prontaentrega.persistence.dto;

import com.prontaentrega.models.Liga;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class LigaAttributeConverter implements AttributeConverter<Liga, String> {
    @Override
    public String convertToDatabaseColumn(Liga liga) {
        return liga == null ? null : liga.value();
    }

    @Override
    public Liga convertToEntityAttribute(String value) {
        return value == null ? null : Liga.fromValue(value);
    }
}
