package com.prontaentrega.schedulers;

import com.prontaentrega.services.PlayerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PlayerScheduler {

    private final PlayerService playerService;

    public PlayerScheduler(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Scheduled(
            cron = "0 0 3 * * SUN",
            zone = "America/Argentina/Buenos_Aires"
    )
    public void refreshPlayerCatalog() {
        playerService.refreshFromWhoScored();
    }
}
