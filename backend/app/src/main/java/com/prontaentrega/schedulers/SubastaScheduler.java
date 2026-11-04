package com.prontaentrega.schedulers;

import com.prontaentrega.services.SubastaService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SubastaScheduler {
    private final SubastaService subastaService;

    public SubastaScheduler(SubastaService subastaService) {
        this.subastaService = subastaService;
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void resolverSubastasVencidas() {
        subastaService.resolverSubastasVencidas();
    }
}
