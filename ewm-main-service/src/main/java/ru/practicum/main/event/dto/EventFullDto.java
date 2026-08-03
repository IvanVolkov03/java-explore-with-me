package ru.practicum.main.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.main.category.dto.CategoryDto;
import java.time.LocalDateTime;

@Getter
@Setter
public class EventFullDto {
    private Long id;
    private String title;
    private String annotation;
    private CategoryDto category;
    private Boolean paid;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private UserShortDto initiator;
    private Long views;
    private Integer confirmedRequests;
    private String description;
    private Integer participantLimit;
    private String state;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;

    private Location location;
    private Boolean requestModeration;
}