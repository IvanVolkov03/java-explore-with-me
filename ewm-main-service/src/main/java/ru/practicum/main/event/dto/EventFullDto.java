package ru.practicum.main.event.dto;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.main.category.dto.CategoryDto;

import java.time.LocalDateTime;

@Getter
@Setter
public class EventFullDto {
    private String annotation;
    private CategoryDto category;
    private Integer confirmedRequests;
    private LocalDateTime createdOn;
    private String description;
    private LocalDateTime eventDate;
    private Long id;
    private UserShortDto initiator;
    private Location location;
    private Boolean paid;
    private Integer participantLimit;
    private LocalDateTime publishedOn;
    private Boolean requestModeration;
    private String state;
    private String title;
    private Long views;
}