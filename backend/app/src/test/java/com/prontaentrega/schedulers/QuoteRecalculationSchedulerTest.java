package com.prontaentrega.schedulers;

import static org.mockito.Mockito.verify;

import com.prontaentrega.services.QuoteRecalculationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Verifica que el disparador semanal delega en el mismo servicio del endpoint manual. */
@ExtendWith(MockitoExtension.class)
class QuoteRecalculationSchedulerTest {
    @Mock
    private QuoteRecalculationService recalculationService;

    /** Invoca el servicio común sin duplicar cálculo en el scheduler. */
    @Test
    void delegaRecotizacionAlServicioComun() {
        QuoteRecalculationScheduler scheduler =
                new QuoteRecalculationScheduler(recalculationService);

        scheduler.recalcularCotizacionesSemanalmente();

        verify(recalculationService).recalcularEstrategiaActiva();
    }
}