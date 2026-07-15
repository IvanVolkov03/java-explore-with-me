package ru.practicum.stats;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.practicum.stats.repository.StatsRepository;

@SpringBootApplication
public class StatsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(StatsServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner clearHits(StatsRepository repository) {
        return args -> repository.deleteAll();
    }
}