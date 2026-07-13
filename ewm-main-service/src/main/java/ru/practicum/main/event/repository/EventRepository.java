package ru.practicum.main.event.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.main.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByInitiatorId(Long userId, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE " +
            "LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "AND e.state = 'PUBLISHED'")
    List<Event> searchByText(@Param("text") String text, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.state = 'PUBLISHED'")
    List<Event> findAllPublished(Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.state = 'PUBLISHED' AND e.eventDate >= :rangeStart")
    List<Event> findAllPublishedAfterDate(@Param("rangeStart") LocalDateTime rangeStart, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.state = 'PUBLISHED' AND e.category.id IN :categories")
    List<Event> findByCategories(@Param("categories") List<Long> categories, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.state = 'PUBLISHED' AND e.paid = :paid")
    List<Event> findByPaid(@Param("paid") Boolean paid, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.initiator.id IN :users")
    List<Event> findByUsers(@Param("users") List<Long> users, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.state IN :states")
    List<Event> findByStates(@Param("states") List<String> states, Pageable pageable);

    @Query("SELECT COUNT(r) FROM ParticipationRequest r WHERE r.event.id = :eventId AND r.status = 'CONFIRMED'")
    Integer countConfirmedRequests(@Param("eventId") Long eventId);
}