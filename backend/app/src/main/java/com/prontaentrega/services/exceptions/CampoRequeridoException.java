package com.prontaentrega.services.exceptions;

public class CampoRequeridoException extends RuntimeException {
    public CampoRequeridoException(String message) {
        super(message);
    }
}
