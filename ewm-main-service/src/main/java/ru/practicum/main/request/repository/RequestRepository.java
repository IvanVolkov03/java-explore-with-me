package ru.practicum.main.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.main.request.model.ParticipationRequest;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findByRequesterId(Long requesterId);

    List<ParticipationRequest> findByEventId(Long eventId);

    Optional<ParticipationRequest> findByEventIdAndRequesterId(Long eventId, Long requesterId);

    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);
}