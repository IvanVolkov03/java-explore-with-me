package ru.practicum.main.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.main.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.main.request.dto.ParticipationRequestDto;
import ru.practicum.main.request.model.ParticipationRequest;
import ru.practicum.main.request.repository.RequestRepository;
import ru.practicum.main.user.model.User;
import ru.practicum.main.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (event.getInitiator().getId().equals(userId)) {
            throw new RuntimeException("Инициатор события не может добавить запрос на участие в своём событии");
        }

        if (!"PUBLISHED".equals(event.getState())) {
            throw new RuntimeException("Нельзя участвовать в неопубликованном событии");
        }

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new RuntimeException("Duplicate request");
        }

        if (event.getParticipantLimit() > 0) {
            Integer confirmedCount = eventRepository.countConfirmedRequests(eventId);
            if (confirmedCount >= event.getParticipantLimit()) {
                throw new RuntimeException("Participant limit reached");
            }
        }

        ParticipationRequest request = new ParticipationRequest();
        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        request.setRequester(requester);

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            request.setStatus("CONFIRMED");
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        } else {
            request.setStatus("PENDING");
        }

        return toDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        return requestRepository.findByRequesterId(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.getRequester().getId().equals(userId)) {
            throw new RuntimeException("Not request owner");
        }

        if ("CONFIRMED".equals(request.getStatus())) {
            Event event = request.getEvent();
            event.setConfirmedRequests(Math.max(0, event.getConfirmedRequests() - 1));
            eventRepository.save(event);
        }

        request.setStatus("CANCELED");
        return toDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new RuntimeException("Not event initiator");
        }

        return requestRepository.findByEventId(eventId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new RuntimeException("Not event initiator");
        }

        List<ParticipationRequest> requests = requestRepository.findAllById(updateRequest.getRequestIds());
        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        Integer currentConfirmed = eventRepository.countConfirmedRequests(eventId);

        for (ParticipationRequest request : requests) {
            if (!"PENDING".equals(request.getStatus())) {
                throw new RuntimeException("Request must have status PENDING");
            }

            if ("CONFIRMED".equals(updateRequest.getStatus())) {
                if (event.getParticipantLimit() > 0 && currentConfirmed >= event.getParticipantLimit()) {
                    request.setStatus("REJECTED");
                    rejected.add(toDto(requestRepository.save(request)));
                } else {
                    request.setStatus("CONFIRMED");
                    confirmed.add(toDto(requestRepository.save(request)));
                    currentConfirmed++;
                }
            } else if ("REJECTED".equals(updateRequest.getStatus())) {
                request.setStatus("REJECTED");
                rejected.add(toDto(requestRepository.save(request)));
            }
        }

        event.setConfirmedRequests(currentConfirmed);

        if (event.getParticipantLimit() > 0 && currentConfirmed >= event.getParticipantLimit()) {
            List<ParticipationRequest> pendingRequests = requestRepository.findByEventId(eventId)
                    .stream()
                    .filter(r -> "PENDING".equals(r.getStatus()))
                    .collect(Collectors.toList());

            for (ParticipationRequest pending : pendingRequests) {
                pending.setStatus("REJECTED");
                rejected.add(toDto(requestRepository.save(pending)));
            }
        }

        eventRepository.save(event);

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(confirmed);
        result.setRejectedRequests(rejected);
        return result;
    }

    private ParticipationRequestDto toDto(ParticipationRequest request) {
        ParticipationRequestDto dto = new ParticipationRequestDto();
        dto.setId(request.getId());
        dto.setCreated(request.getCreated());
        dto.setEvent(request.getEvent().getId());
        dto.setRequester(request.getRequester().getId());
        dto.setStatus(request.getStatus());
        return dto;
    }
}