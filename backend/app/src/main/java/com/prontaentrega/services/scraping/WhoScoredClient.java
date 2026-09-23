package com.prontaentrega.services.scraping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prontaentrega.services.dto.external.WhoScoredResponse;
import com.prontaentrega.services.exceptions.ProviderUnavailableException;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import java.net.CookieManager;
import java.net.CookiePolicy;


/**
 * Cliente HTTP responsable de obtener estadisticas de jugadores desde WhoScored.
 */
@Component
public class WhoScoredClient {

    private final WhoScoredProperties properties;
    private final CookieManager cookieManager = new CookieManager();
    private final ObjectMapper objectMapper;

    /**
     * Construye el cliente con la configuracion externa de URL y timeout.
     */
    public WhoScoredClient(WhoScoredProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    }

    /**
     * Recupera una pagina de jugadores desde WhoScored.
     *
     * @param page pagina a consultar
     * @param pageSize cantidad de jugadores por pagina
     * @return respuesta de WhoScored con jugadores y datos de paginacion
     */
    public WhoScoredResponse fetchPlayerStatsPage(int page, int pageSize) {
        if (properties.url() == null || properties.url().isBlank()) {
            throw new ProviderUnavailableException(
                    "WHOSCORED_URL_INVALIDA",
                    "No se pudo completar la actualizacion. La URL de WhoScored no esta configurada."
            );
        }

        try {
            RestClient restClient = RestClient.builder()
                    .requestFactory(requestFactory())
                    .build();

            String url = properties.url()
                    .replaceAll("([?&])page=[^&]*", "$1page=" + page)
                    .replaceAll("([?&])numberOfPlayersToPick=[^&]*", "$1numberOfPlayersToPick=" + pageSize);

            String body = restClient.get()
                    .uri(url)
                    .header("User-Agent",
                            "Mozilla/5.0 (Linux; Android 15; Pixel 9) AppleWebKit/537.36 " +
                                    "(KHTML, like Gecko) Chrome/152.0.0.0 Mobile Safari/537.3")
                    .header("Accept",
                            "text/html,application/xhtml+xml,application/xml;q=0.9," +
                                    "image/avif,image/webp,image/apng,*/*;q=0.8," +
                                    "application/signed-exchange;v=b3;q=0.7")
                    .header("Referer", "https://www.whoscored.com/")
                    .header("Sec-Fetch-Dest", "empty")
                    .header("Sec-Fetch-Mode", "cors")
                    .header("Sec-Fetch-Site", "same-origin")
                    .retrieve()
                    .body(String.class);

            if (body == null || body.isBlank()) {
                throw new ProviderUnavailableException(
                        "WHOSCORED_RESPUESTA_INVALIDA",
                        "No se pudo completar la actualizacion. WhoScored no entrego datos utilizables."
                );
            }

            WhoScoredResponse response =
                    objectMapper.readValue(body, WhoScoredResponse.class);

            if (response == null) {
                throw new ProviderUnavailableException(
                        "WHOSCORED_RESPUESTA_INVALIDA",
                        "No se pudo completar la actualizacion. WhoScored no entrego datos utilizables."
                );
            }

            return response;

        } catch (ProviderUnavailableException ex) {
            //Este catch es raro, habria que mejorarlo :P
            throw ex;
        } catch (JsonProcessingException ex) {
            throw new ProviderUnavailableException(
                    "WHOSCORED_RESPUESTA_INVALIDA",
                    "No se pudo completar la actualizacion. WhoScored devolvio una respuesta no utilizable."
            );
        } catch (RestClientException ex) {
            throw new ProviderUnavailableException(
                    "WHOSCORED_NO_DISPONIBLE",
                    "No se pudo completar la actualizacion. Se conserva la informacion local vigente."
            );
        }
    }

    private JdkClientHttpRequestFactory requestFactory() {
        Duration timeout = Duration.ofMillis(properties.safeTimeoutMs());

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .cookieHandler(cookieManager)
                .build();

        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);

        requestFactory.setReadTimeout(timeout);

        return requestFactory;
    }
}
