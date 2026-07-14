package ru.practicum.main.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class NewCompilationDto {
    private List<Long> events;
    private Boolean pinned;

    @NotBlank
    @Size(min = 1, max = 50)
    private String title;
}