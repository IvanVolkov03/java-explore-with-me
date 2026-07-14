package ru.practicum.main.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.category.repository.CategoryRepository;
import ru.practicum.main.event.dto.*;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.user.model.User;
import ru.practicum.main.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final StatsClient statsClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with id=" + newEventDto.getCategory() + " was not found"));

        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new IllegalArgumentException("Field: eventDate. Error: должно содержать дату, которая еще не наступила");
        }

        Event event = new Event();
        event.setAnnotation(newEventDto.getAnnotation());
        event.setCategory(category);
        event.setConfirmedRequests(0);
        event.setCreatedOn(LocalDateTime.now());
        event.setDescription(newEventDto.getDescription());
        event.setEventDate(newEventDto.getEventDate());
        event.setInitiator(initiator);
        event.setLocationLat(newEventDto.getLocation().getLat());
        event.setLocationLon(newEventDto.getLocation().getLon());
        event.setPaid(newEventDto.getPaid() != null ? newEventDto.getPaid() : false);
        event.setParticipantLimit(newEventDto.getParticipantLimit() != null ? newEventDto.getParticipantLimit() : 0);
        event.setRequestModeration(newEventDto.getRequestModeration() != null ? newEventDto.getRequestModeration() : true);
        event.setState("PENDING");
        event.setTitle(newEventDto.getTitle());
        event.setViews(0);

        return toFullDto(eventRepository.save(event), 0L, 0);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateEventRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("User with id=" + userId + " is not the initiator of the event");
        }

        if (!"PENDING".equals(event.getState()) && !"CANCELED".equals(event.getState())) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        if (updateEventRequest.getEventDate() != null &&
                updateEventRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new IllegalArgumentException("Field: eventDate. Error: должно содержать дату, которая еще не наступила");
        }

        updateEventUser(event, updateEventRequest);
        return toFullDto(eventRepository.save(event), 0L, 0);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (updateEventRequest.getEventDate() != null && updateEventRequest.getEventDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Field: eventDate. Error: должно содержать дату, которая еще не наступила");
        }

        updateEventAdmin(event, updateEventRequest);
        return toFullDto(eventRepository.save(event), 0L, 0);
    }

    @Override
    public List<EventShortDto> getEventsByUser(Long userId, Integer from, Integer size) {
        List<Event> events = eventRepository.findByInitiatorId(userId, PageRequest.of(from / size, size));
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> viewsMap = getViewsMap(eventIds);

        return events.stream()
                .map(e -> toShortDto(e, viewsMap.getOrDefault(e.getId(), 0L),
                        eventRepository.countConfirmedRequests(e.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventByUser(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("User with id=" + userId + " is not the initiator of the event");
        }

        Long views = getViews(eventId);
        Integer confirmedRequests = eventRepository.countConfirmedRequests(eventId);
        return toFullDto(event, views, confirmedRequests);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventByIdPublic(Long eventId, String ip, String uri) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!"PUBLISHED".equals(event.getState())) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }

        saveHit(ip, uri);
        Long views = getViews(eventId);
        Integer confirmedRequests = eventRepository.countConfirmedRequests(eventId);
        return toFullDto(event, views, confirmedRequests);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsPublic(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Boolean onlyAvailable, Boolean unique, String sort,
                                               Integer from, Integer size, String ip, String uri) {
        final LocalDateTime finalRangeStart = rangeStart != null ? rangeStart : LocalDateTime.now();
        final LocalDateTime finalRangeEnd = rangeEnd;

        if (finalRangeEnd != null && finalRangeEnd.isBefore(finalRangeStart)) {
            throw new IllegalArgumentException("rangeEnd must be after rangeStart");
        }

        List<Event> events = eventRepository.findAll().stream()
                .filter(e -> "PUBLISHED".equals(e.getState()))
                .collect(Collectors.toList());

        if (text != null && !text.isEmpty()) {
            events = events.stream()
                    .filter(e -> e.getAnnotation().toLowerCase().contains(text.toLowerCase()) ||
                            e.getDescription().toLowerCase().contains(text.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (categories != null && !categories.isEmpty()) {
            events = events.stream()
                    .filter(e -> categories.contains(e.getCategory().getId()))
                    .collect(Collectors.toList());
        }

        if (paid != null) {
            events = events.stream()
                    .filter(e -> e.getPaid().equals(paid))
                    .collect(Collectors.toList());
        }

        events = events.stream()
                .filter(e -> !e.getEventDate().isBefore(finalRangeStart))
                .collect(Collectors.toList());

        if (finalRangeEnd != null) {
            events = events.stream()
                    .filter(e -> !e.getEventDate().isAfter(finalRangeEnd))
                    .collect(Collectors.toList());
        }

        if (onlyAvailable != null && onlyAvailable) {
            events = events.stream()
                    .filter(e -> e.getParticipantLimit() == null || e.getParticipantLimit() == 0 ||
                            e.getConfirmedRequests() < e.getParticipantLimit())
                    .collect(Collectors.toList());
        }

        saveHit(ip, uri);

        int start = from;
        int end = Math.min(from + size, events.size());
        List<Event> pagedEvents = events.subList(start, end);

        List<Long> eventIds = pagedEvents.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> viewsMap = getViewsMap(eventIds);

        List<EventShortDto> result = pagedEvents.stream()
                .map(e -> {
                    Long views = viewsMap.getOrDefault(e.getId(), 0L);
                    Integer confirmedRequests = eventRepository.countConfirmedRequests(e.getId());
                    return toShortDto(e, views, confirmedRequests);
                })
                .collect(Collectors.toList());

        if ("VIEWS".equals(sort)) {
            result.sort((a, b) -> Long.compare(b.getViews(), a.getViews()));
        } else if ("EVENT_DATE".equals(sort)) {
            result.sort(Comparator.comparing(EventShortDto::getEventDate));
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getEventsByAdmin(List<Long> users, List<String> states, List<Long> categories,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Integer from, Integer size) {
        LocalDateTime finalRangeStart = rangeStart;
        LocalDateTime finalRangeEnd = rangeEnd;

        Page<Event> eventPage = eventRepository.findAll(PageRequest.of(from / size, size));
        List<Event> events = eventPage.getContent();

        if (users != null && !users.isEmpty()) {
            events = events.stream()
                    .filter(e -> users.contains(e.getInitiator().getId()))
                    .collect(Collectors.toList());
        }

        if (states != null && !states.isEmpty()) {
            events = events.stream()
                    .filter(e -> states.contains(e.getState()))
                    .collect(Collectors.toList());
        }

        if (categories != null && !categories.isEmpty()) {
            events = events.stream()
                    .filter(e -> categories.contains(e.getCategory().getId()))
                    .collect(Collectors.toList());
        }

        if (finalRangeStart != null) {
            events = events.stream()
                    .filter(e -> !e.getEventDate().isBefore(finalRangeStart))
                    .collect(Collectors.toList());
        }

        if (finalRangeEnd != null) {
            events = events.stream()
                    .filter(e -> !e.getEventDate().isAfter(finalRangeEnd))
                    .collect(Collectors.toList());
        }

        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> viewsMap = getViewsMap(eventIds);

        return events.stream()
                .map(e -> toFullDto(e, viewsMap.getOrDefault(e.getId(), 0L),
                        eventRepository.countConfirmedRequests(e.getId())))
                .collect(Collectors.toList());
    }

    private void updateEventUser(Event event, UpdateEventUserRequest updateEventRequest) {
        if (updateEventRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventRequest.getAnnotation());
        }
        if (updateEventRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id=" + updateEventRequest.getCategory() + " was not found"));
            event.setCategory(category);
        }
        if (updateEventRequest.getDescription() != null) {
            event.setDescription(updateEventRequest.getDescription());
        }
        if (updateEventRequest.getEventDate() != null) {
            event.setEventDate(updateEventRequest.getEventDate());
        }
        if (updateEventRequest.getLocation() != null) {
            event.setLocationLat(updateEventRequest.getLocation().getLat());
            event.setLocationLon(updateEventRequest.getLocation().getLon());
        }
        if (updateEventRequest.getPaid() != null) {
            event.setPaid(updateEventRequest.getPaid());
        }
        if (updateEventRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventRequest.getParticipantLimit());
        }
        if (updateEventRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventRequest.getRequestModeration());
        }
        if (updateEventRequest.getTitle() != null) {
            event.setTitle(updateEventRequest.getTitle());
        }
        if (updateEventRequest.getStateAction() != null) {
            if ("SEND_TO_REVIEW".equals(updateEventRequest.getStateAction())) {
                event.setState("PENDING");
            } else if ("CANCEL_REVIEW".equals(updateEventRequest.getStateAction())) {
                event.setState("CANCELED");
            }
        }
    }

    private void updateEventAdmin(Event event, UpdateEventAdminRequest updateEventRequest) {
        if (updateEventRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventRequest.getAnnotation());
        }
        if (updateEventRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id=" + updateEventRequest.getCategory() + " was not found"));
            event.setCategory(category);
        }
        if (updateEventRequest.getDescription() != null) {
            event.setDescription(updateEventRequest.getDescription());
        }
        if (updateEventRequest.getEventDate() != null) {
            event.setEventDate(updateEventRequest.getEventDate());
        }
        if (updateEventRequest.getLocation() != null) {
            event.setLocationLat(updateEventRequest.getLocation().getLat());
            event.setLocationLon(updateEventRequest.getLocation().getLon());
        }
        if (updateEventRequest.getPaid() != null) {
            event.setPaid(updateEventRequest.getPaid());
        }
        if (updateEventRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventRequest.getParticipantLimit());
        }
        if (updateEventRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventRequest.getRequestModeration());
        }
        if (updateEventRequest.getTitle() != null) {
            event.setTitle(updateEventRequest.getTitle());
        }
        if (updateEventRequest.getStateAction() != null) {
            if ("PUBLISH_EVENT".equals(updateEventRequest.getStateAction())) {
                if (!"PENDING".equals(event.getState())) {
                    throw new ConflictException("Cannot publish the event because it's not in the right state: " + event.getState());
                }
                event.setState("PUBLISHED");
                event.setPublishedOn(LocalDateTime.now());
            } else if ("REJECT_EVENT".equals(updateEventRequest.getStateAction())) {
                if ("PUBLISHED".equals(event.getState())) {
                    throw new ConflictException("Cannot reject published event");
                }
                event.setState("CANCELED");
            }
        }
    }

    private void saveHit(String ip, String uri) {
        try {
            EndpointHit hit = new EndpointHit();
            hit.setApp("ewm-main-service");
            hit.setUri(uri);
            hit.setIp(ip);
            hit.setTimestamp(LocalDateTime.now());
            statsClient.saveHit(hit);
        } catch (Exception e) {
            log.error("Failed to save hit for uri {}: {}", uri, e.getMessage(), e);
        }
    }

    private Long getViews(Long eventId) {
        try {
            String start = LocalDateTime.now().minusYears(10).format(FORMATTER);
            String end = LocalDateTime.now().plusDays(1).format(FORMATTER);
            List<ViewStats> stats = statsClient.getStats(
                    start,
                    end,
                    List.of("/events/" + eventId),
                    true
            ).getBody();
            return stats != null && !stats.isEmpty() ? stats.get(0).getHits() : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    private Map<Long, Long> getViewsMap(List<Long> eventIds) {
        Map<Long, Long> viewsMap = new HashMap<>();
        if (eventIds.isEmpty()) {
            return viewsMap;
        }
        try {
            String start = LocalDateTime.now().minusYears(10).format(FORMATTER);
            String end = LocalDateTime.now().plusDays(1).format(FORMATTER);
            List<String> uris = eventIds.stream()
                    .map(id -> "/events/" + id)
                    .collect(Collectors.toList());

            List<ViewStats> stats = statsClient.getStats(
                    start,
                    end,
                    uris,
                    false
            ).getBody();

            if (stats != null) {
                for (ViewStats stat : stats) {
                    Long id = Long.parseLong(stat.getUri().replace("/events/", ""));
                    viewsMap.put(id, stat.getHits());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get views map: {}", e.getMessage());
        }
        return viewsMap;
    }

    private EventFullDto toFullDto(Event event, Long views, Integer confirmedRequests) {
        EventFullDto dto = new EventFullDto();
        dto.setId(event.getId());
        dto.setAnnotation(event.getAnnotation());

        ru.practicum.main.category.dto.CategoryDto categoryDto = new ru.practicum.main.category.dto.CategoryDto();
        categoryDto.setId(event.getCategory().getId());
        categoryDto.setName(event.getCategory().getName());
        dto.setCategory(categoryDto);

        dto.setConfirmedRequests(confirmedRequests);
        dto.setCreatedOn(event.getCreatedOn());
        dto.setDescription(event.getDescription());
        dto.setEventDate(event.getEventDate());

        UserShortDto initiatorDto = new UserShortDto();
        initiatorDto.setId(event.getInitiator().getId());
        initiatorDto.setName(event.getInitiator().getName());
        dto.setInitiator(initiatorDto);

        Location location = new Location();
        location.setLat(event.getLocationLat());
        location.setLon(event.getLocationLon());
        dto.setLocation(location);

        dto.setPaid(event.getPaid());
        dto.setParticipantLimit(event.getParticipantLimit());
        dto.setPublishedOn(event.getPublishedOn());
        dto.setRequestModeration(event.getRequestModeration());
        dto.setState(event.getState());
        dto.setTitle(event.getTitle());
        dto.setViews(views);
        return dto;
    }

    private EventShortDto toShortDto(Event event, Long views, Integer confirmedRequests) {
        EventShortDto dto = new EventShortDto();
        dto.setId(event.getId());
        dto.setAnnotation(event.getAnnotation());

        ru.practicum.main.category.dto.CategoryDto categoryDto = new ru.practicum.main.category.dto.CategoryDto();
        categoryDto.setId(event.getCategory().getId());
        categoryDto.setName(event.getCategory().getName());
        dto.setCategory(categoryDto);

        dto.setConfirmedRequests(confirmedRequests);
        dto.setEventDate(event.getEventDate());
        dto.setParticipantLimit(event.getParticipantLimit());

        UserShortDto initiatorDto = new UserShortDto();
        initiatorDto.setId(event.getInitiator().getId());
        initiatorDto.setName(event.getInitiator().getName());
        dto.setInitiator(initiatorDto);

        dto.setPaid(event.getPaid());
        dto.setTitle(event.getTitle());
        dto.setViews(views);
        return dto;
    }
}