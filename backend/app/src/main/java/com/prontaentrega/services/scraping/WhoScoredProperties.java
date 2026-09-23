package com.prontaentrega.services.scraping;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuracion de acceso al proveedor WhoScored.
 */
@ConfigurationProperties(prefix = "providers.whoscored")
public record WhoScoredProperties(String url, int timeoutMs) {
    /**
     * Devuelve el timeout configurado o un valor conservador cuando no se especifica.
     */
    public int safeTimeoutMs() {
        return timeoutMs > 0 ? timeoutMs : 5000;
    }
}
