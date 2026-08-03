package ru.practicum.main.event.service;

import ru.practicum.main.event.dto.*;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventFullDto addEvent(Long userId, NewEventDto newEventDto);

    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateEventRequest);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventRequest);

    List<EventShortDto> getEventsByUser(Long userId, Integer from, Integer size);

    EventFullDto getEventByUser(Long userId, Long eventId);

    EventFullDto getEventByIdPublic(Long eventId, String ip, String uri);

    List<EventShortDto> getEventsPublic(String text, List<Long> categories, Boolean paid,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                        Boolean onlyAvailable, Boolean unique, String sort,
                                        Integer from, Integer size, String ip, String uri);

    List<EventFullDto> getEventsByAdmin(List<Long> users, List<String> states, List<Long> categories,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                        Integer from, Integer size);
}