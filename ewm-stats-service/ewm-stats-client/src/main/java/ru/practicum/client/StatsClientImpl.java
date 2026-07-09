package ru.practicum.client;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;

@Component
public class StatsClientImpl implements StatsClient {

    private final WebClient webClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClientImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:9090").build();
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
    public ResponseEntity<List<ViewStats>> getStats(String start, String end,
                                                    List<String> uris, Boolean unique) {
        String urisParam = "";
        if (uris != null && !uris.isEmpty()) {
            urisParam = "&uris=" + String.join("&uris=", uris);
        }

        return webClient.get()
                .uri("/stats?start={start}&end={end}{uris}&unique={unique}",
                        URLEncoder.encode(start, StandardCharsets.UTF_8),
                        URLEncoder.encode(end, StandardCharsets.UTF_8),
                        urisParam,
                        unique)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ViewStats>>() {})
                .map(ResponseEntity::ok)
                .block();
    }
}