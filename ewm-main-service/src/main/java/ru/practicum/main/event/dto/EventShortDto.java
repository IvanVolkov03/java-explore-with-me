package ru.practicum.main.event.dto;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.main.category.dto.CategoryDto;

import java.time.LocalDateTime;

@Getter
@Setter
public class EventShortDto {
    private String annotation;
    private CategoryDto category;
    private Integer confirmedRequests;
    private LocalDateTime eventDate;
    private Long id;
    private UserShortDto initiator;
    private Integer participantLimit;
    private Boolean paid;
    private String title;
    private Long views;
}