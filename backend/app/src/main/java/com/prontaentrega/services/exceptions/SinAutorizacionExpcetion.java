package com.prontaentrega.services.exceptions;

public class SinAutorizacionExpcetion extends RuntimeException {
    public SinAutorizacionExpcetion(String message) {
        super(message);
    }
}
