package ru.practicum.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;

import java.util.List;

public class StatsClientImpl implements StatsClient {

    private final WebClient webClient;

    public StatsClientImpl(WebClient.Builder webClientBuilder, String statsServerUrl) {
        this.webClient = webClientBuilder.baseUrl(statsServerUrl).build();
    }

    @Override
    public ResponseEntity<Object> saveHit(EndpointHit hit) {
        return webClient.post()
                .uri("/hit")
                .bodyValue(hit)
                .retrieve()
                .toEntity(Object.class)
                .block();
    }

    @Override
    public ResponseEntity<List<ViewStats>> getStats(String start, String end, List<String> uris, Boolean unique) {
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/stats")
                            .queryParam("start", start)
                            .queryParam("end", end)
                            .queryParam("unique", unique);
                    if (uris != null && !uris.isEmpty()) {
                        for (String uri : uris) {
                            uriBuilder.queryParam("uris", uri);
                        }
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ViewStats>>() {})
                .map(ResponseEntity::ok)
                .block();
    }
}