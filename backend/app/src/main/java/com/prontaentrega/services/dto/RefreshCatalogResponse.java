package com.prontaentrega.services.dto;

/**
 * Respuesta publica del proceso de actualizacion del catalogo de jugadores.
 */
public record RefreshCatalogResponse(boolean success, String message, int created, int updated, int skipped,
                                     String source) {
    /**
     * Construye una respuesta exitosa con los contadores del proceso.
     */
    public static RefreshCatalogResponse success(int created, int updated, int skipped) {
        return new RefreshCatalogResponse(true, "Actualizacion completada correctamente", created, updated,
                skipped, "WhoScored");
    }

    /**
     * Construye una respuesta de falla que confirma que los datos locales se preservaron.
     */
    public static RefreshCatalogResponse failure(String message) {
        return new RefreshCatalogResponse(false, message, 0, 0, 0, "WhoScored");
    }
}
