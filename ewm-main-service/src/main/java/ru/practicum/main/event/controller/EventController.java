package ru.practicum.main.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.event.dto.*;
import ru.practicum.main.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping("/events")
    public List<EventShortDto> getEventsPublic(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(defaultValue = "false") Boolean unique,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {

        return eventService.getEventsPublic(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, unique, sort, from, size, request.getRemoteAddr(), request.getRequestURI());
    }

    @GetMapping("/events/{id}")
    public EventFullDto getEventByIdPublic(@PathVariable Long id, HttpServletRequest request) {
        return eventService.getEventByIdPublic(id, request.getRemoteAddr(), request.getRequestURI());
    }

    @GetMapping("/admin/events")
    public List<EventFullDto> getEventsByAdmin(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {

        return eventService.getEventsByAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/admin/events/{eventId}")
    public EventFullDto updateEventByAdmin(@PathVariable Long eventId,
                                           @Valid @RequestBody UpdateEventAdminRequest updateEventRequest) {
        return eventService.updateEventByAdmin(eventId, updateEventRequest);
    }

    @GetMapping("/users/{userId}/events")
    public List<EventShortDto> getEventsByUser(@PathVariable Long userId,
                                               @RequestParam(defaultValue = "0") Integer from,
                                               @RequestParam(defaultValue = "10") Integer size) {
        return eventService.getEventsByUser(userId, from, size);
    }

    @PostMapping("/users/{userId}/events")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable Long userId,
                                 @Valid @RequestBody NewEventDto newEventDto) {
        return eventService.addEvent(userId, newEventDto);
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    public EventFullDto getEventByUser(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventService.getEventByUser(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public EventFullDto updateEventByUser(@PathVariable Long userId, @PathVariable Long eventId,
                                          @Valid @RequestBody UpdateEventUserRequest updateEventRequest) {
        return eventService.updateEventByUser(userId, eventId, updateEventRequest);
    }
}