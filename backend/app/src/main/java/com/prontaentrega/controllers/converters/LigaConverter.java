package com.prontaentrega.controllers.converters;

import com.prontaentrega.models.Liga;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class LigaConverter implements Converter<String, Liga> {
    @Override
    public Liga convert(String source) {
        return Liga.fromValue(source);
    }
}
