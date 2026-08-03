package ru.practicum.main.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import ru.practicum.client.StatsClient;
import ru.practicum.client.StatsClientImpl;

@Slf4j
@Configuration
public class StatsClientConfig {

    @Value("${STATS_SERVER_URL:http://localhost:9090}")
    private String statsServerUrl;

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public StatsClient statsClient(WebClient.Builder webClientBuilder) {
        log.info("=== INITIALIZING STATS CLIENT WITH URL: {} ===", statsServerUrl);
        return new StatsClientImpl(webClientBuilder, statsServerUrl);
    }
}