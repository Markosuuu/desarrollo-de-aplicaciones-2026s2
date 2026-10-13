package com.prontaentrega.services.scraping;

import com.prontaentrega.services.dto.external.PlayerScrapperResponse;

public interface PlayerScrapper {
    PlayerScrapperResponse fetchPlayerStatsPage(int page, int pageSize);
    String source();
}
