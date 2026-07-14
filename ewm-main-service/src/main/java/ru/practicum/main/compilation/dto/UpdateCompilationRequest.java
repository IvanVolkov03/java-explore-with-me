package ru.practicum.main.compilation.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateCompilationRequest {
    private List<Long> events;
    private Boolean pinned;

    @Size(max = 50)
    private String title;
}