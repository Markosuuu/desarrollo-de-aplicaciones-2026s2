package com.prontaentrega.services.exceptions;

public class CreditosInsuficientesException extends RuntimeException {
    public CreditosInsuficientesException(String message) {
        super(message);
    }
}
