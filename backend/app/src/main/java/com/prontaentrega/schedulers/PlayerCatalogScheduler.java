package com.prontaentrega.schedulers;

import com.prontaentrega.services.PlayerCatalogService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PlayerCatalogScheduler {

    private final PlayerCatalogService playerCatalogService;

    public PlayerCatalogScheduler(PlayerCatalogService playerCatalogService) {
        this.playerCatalogService = playerCatalogService;
    }

    @Scheduled(
            cron = "0 0 3 * * SUN",
            zone = "America/Argentina/Buenos_Aires"
    )
    public void refreshPlayerCatalog() {
        playerCatalogService.refreshFromWhoScored();
    }
}
