package ru.practicum.stats.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.stats.model.Hit;
import ru.practicum.stats.repository.StatsRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Override
    @Transactional
    public void saveHit(EndpointHit endpointHit) {
        Hit hit = Hit.builder()
                .app(endpointHit.getApp())
                .uri(endpointHit.getUri())
                .ip(endpointHit.getIp())
                .timestamp(endpointHit.getTimestamp())
                .build();
        statsRepository.save(hit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end,
                                    List<String> uris, Boolean unique) {
        List<Object[]> results;

        if (uris != null && !uris.isEmpty()) {
            if (unique) {
                results = statsRepository.findStatsWithUrisAndUniqueTrue(start, end, uris);
            } else {
                results = statsRepository.findStatsWithUrisAndUniqueFalse(start, end, uris);
            }
        } else {
            if (unique) {
                results = statsRepository.findStatsWithUniqueTrue(start, end);
            } else {
                results = statsRepository.findStatsWithUniqueFalse(start, end);
            }
        }

        return results.stream()
                .map(row -> ViewStats.builder()
                        .app((String) row[0])
                        .uri((String) row[1])
                        .hits((Long) row[2])
                        .build())
                .collect(Collectors.toList());
    }
}