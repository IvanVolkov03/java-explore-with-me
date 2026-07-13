package ru.practicum.main.compilation.dto;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.main.event.dto.EventShortDto;

import java.util.Set;

@Getter
@Setter
public class CompilationDto {
    private Long id;
    private Boolean pinned;
    private String title;
    private Set<EventShortDto> events;
}