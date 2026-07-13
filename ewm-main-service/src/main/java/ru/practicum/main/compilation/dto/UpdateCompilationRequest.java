package ru.practicum.main.compilation.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UpdateCompilationRequest {
    private Set<Long> events;

    private Boolean pinned;

    private String title;
}