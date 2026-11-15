package com.prontaentrega.services.exceptions.mercado;

public class CreditosInsuficientesException extends RuntimeException {
    public CreditosInsuficientesException(String message) {
        super(message);
    }
}
