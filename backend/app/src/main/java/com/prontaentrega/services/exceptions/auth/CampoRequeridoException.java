package com.prontaentrega.services.exceptions.auth;

public class CampoRequeridoException extends RuntimeException {
    public CampoRequeridoException(String message) {
        super(message);
    }
}
