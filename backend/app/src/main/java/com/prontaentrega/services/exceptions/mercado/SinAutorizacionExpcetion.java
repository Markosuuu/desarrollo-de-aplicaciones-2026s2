package com.prontaentrega.services.exceptions.mercado;

public class SinAutorizacionExpcetion extends RuntimeException {
    public SinAutorizacionExpcetion(String message) {
        super(message);
    }
}
