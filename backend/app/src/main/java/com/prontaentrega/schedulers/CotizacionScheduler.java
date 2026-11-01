package com.prontaentrega.schedulers;

import com.prontaentrega.services.CotizacionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Dispara semanalmente el mismo servicio de recálculo usado por la operación manual. */
@Component
public class CotizacionScheduler {
    private final CotizacionService administrationService;

    /** Construye el scheduler con el servicio transaccional de cotizaciones. */
    public CotizacionScheduler(CotizacionService administrationService) {
        this.administrationService = administrationService;
    }

    /** Ejecuta la recotización los domingos a las 03:00 de Buenos Aires. */
    @Scheduled(cron = "0 0 3 * * SUN", zone = "America/Argentina/Buenos_Aires")
    public void recalcularCotizacionesSemanalmente() {
        administrationService.recalcularEstrategiaActiva();
    }
}