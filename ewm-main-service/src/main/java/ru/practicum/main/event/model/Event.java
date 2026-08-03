package ru.practicum.main.event.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "annotation", nullable = false, length = 2000)
    private String annotation;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "confirmed_requests", nullable = false)
    private Integer confirmedRequests = 0;

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;

    @Column(name = "description", nullable = false, length = 7000)
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @ManyToOne
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @Column(name = "location_lat", nullable = false)
    private Float locationLat;

    @Column(name = "location_lon", nullable = false)
    private Float locationLon;

    @Column(name = "paid", nullable = false)
    private Boolean paid;

    @Column(name = "participant_limit", nullable = false)
    private Integer participantLimit = 0;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @Column(name = "views", nullable = false)
    private Integer views = 0;

    @Column(name = "request_moderation", nullable = false)
    private Boolean requestModeration = true;
}