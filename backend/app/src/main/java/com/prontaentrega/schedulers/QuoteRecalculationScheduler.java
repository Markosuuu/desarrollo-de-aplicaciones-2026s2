package com.prontaentrega.schedulers;

import com.prontaentrega.services.QuoteRecalculationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Dispara semanalmente el mismo servicio de recálculo usado por la operación manual. */
@Component
public class QuoteRecalculationScheduler {
    private final QuoteRecalculationService recalculationService;

    /** Construye el scheduler con el servicio transaccional de cotizaciones. */
    public QuoteRecalculationScheduler(QuoteRecalculationService recalculationService) {
        this.recalculationService = recalculationService;
    }

    /** Ejecuta la recotización los domingos a las 03:00 de Buenos Aires. */
    @Scheduled(cron = "0 0 3 * * SUN", zone = "America/Argentina/Buenos_Aires")
    public void recalcularCotizacionesSemanalmente() {
        recalculationService.recalcularEstrategiaActiva();
    }
}