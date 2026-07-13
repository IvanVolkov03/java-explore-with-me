package ru.practicum.main.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.compilation.dto.CompilationDto;
import ru.practicum.main.compilation.dto.NewCompilationDto;
import ru.practicum.main.compilation.dto.UpdateCompilationRequest;
import ru.practicum.main.compilation.model.Compilation;
import ru.practicum.main.compilation.repository.CompilationRepository;
import ru.practicum.main.event.dto.EventShortDto;
import ru.practicum.main.event.dto.UserShortDto;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.repository.EventRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = new Compilation();
        compilation.setPinned(newCompilationDto.getPinned());
        compilation.setTitle(newCompilationDto.getTitle());

        Set<Event> events = new HashSet<>();
        if (newCompilationDto.getEvents() != null) {
            events = eventRepository.findAllById(newCompilationDto.getEvents())
                    .stream()
                    .filter(e -> "PUBLISHED".equals(e.getState()))
                    .collect(Collectors.toSet());
        }
        compilation.setEvents(events);

        return toDto(compilationRepository.save(compilation));
    }

    @Override
    public void deleteCompilation(Long compId) {
        compilationRepository.deleteById(compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new RuntimeException("Compilation not found"));

        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
        if (updateCompilationRequest.getEvents() != null) {
            Set<Event> events = eventRepository.findAllById(updateCompilationRequest.getEvents())
                    .stream()
                    .filter(e -> "PUBLISHED".equals(e.getState()))
                    .collect(Collectors.toSet());
            compilation.setEvents(events);
        }

        return toDto(compilationRepository.save(compilation));
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        List<Compilation> compilations = compilationRepository.findAll();

        if (pinned != null) {
            compilations = compilations.stream()
                    .filter(c -> c.getPinned().equals(pinned))
                    .collect(Collectors.toList());
        }

        int start = from;
        int end = Math.min(from + size, compilations.size());

        if (start > compilations.size()) {
            return List.of();
        }

        return compilations.subList(start, end)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new RuntimeException("Compilation not found"));
        return toDto(compilation);
    }

    private CompilationDto toDto(Compilation compilation) {
        CompilationDto dto = new CompilationDto();
        dto.setId(compilation.getId());
        dto.setPinned(compilation.getPinned());
        dto.setTitle(compilation.getTitle());

        Set<EventShortDto> eventDtos = compilation.getEvents() != null ?
                compilation.getEvents().stream()
                        .map(e -> {
                            EventShortDto shortDto = new EventShortDto();
                            shortDto.setId(e.getId());
                            shortDto.setAnnotation(e.getAnnotation());
                            shortDto.setTitle(e.getTitle());
                            shortDto.setPaid(e.getPaid());
                            shortDto.setEventDate(e.getEventDate());

                            ru.practicum.main.category.dto.CategoryDto catDto = new ru.practicum.main.category.dto.CategoryDto();
                            catDto.setId(e.getCategory().getId());
                            catDto.setName(e.getCategory().getName());
                            shortDto.setCategory(catDto);

                            UserShortDto userDto = new UserShortDto();
                            userDto.setId(e.getInitiator().getId());
                            userDto.setName(e.getInitiator().getName());
                            shortDto.setInitiator(userDto);

                            shortDto.setConfirmedRequests(e.getConfirmedRequests());
                            shortDto.setViews((long) e.getViews());

                            return shortDto;
                        })
                        .collect(Collectors.toSet()) : new HashSet<>();

        dto.setEvents(eventDtos);
        return dto;
    }
}