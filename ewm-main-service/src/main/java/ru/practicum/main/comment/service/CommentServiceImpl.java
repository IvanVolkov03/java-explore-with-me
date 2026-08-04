package ru.practicum.main.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.comment.dto.AdminCommentDto;
import ru.practicum.main.comment.dto.CommentDto;
import ru.practicum.main.comment.dto.NewCommentDto;
import ru.practicum.main.comment.dto.UpdateCommentDto;
import ru.practicum.main.comment.dto.UpdateCommentStatusDto;
import ru.practicum.main.comment.model.Comment;
import ru.practicum.main.comment.model.CommentStatus;
import ru.practicum.main.comment.repository.CommentRepository;
import ru.practicum.main.event.dto.UserShortDto;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.ForbiddenException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.user.model.User;
import ru.practicum.main.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!"PUBLISHED".equals(event.getState())) {
            throw new ConflictException("Cannot comment on unpublished event");
        }

        Comment comment = new Comment();
        comment.setText(newCommentDto.getText());
        comment.setEvent(event);
        comment.setAuthor(author);
        comment.setCreatedOn(LocalDateTime.now());
        comment.setStatus(CommentStatus.PUBLISHED);

        return toDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto updateCommentDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("User is not the author of the comment");
        }

        if (comment.getStatus() != CommentStatus.PUBLISHED) {
            throw new ConflictException("Cannot update comment that is not published");
        }

        if (updateCommentDto.getText() != null) {
            comment.setText(updateCommentDto.getText());
        }

        return toDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("User is not the author of the comment");
        }

        commentRepository.delete(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByEvent(Long eventId, Integer from, Integer size) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }

        return commentRepository.findByEventIdAndStatus(eventId, CommentStatus.PUBLISHED, PageRequest.of(from / size, size))
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByUser(Long userId, Integer from, Integer size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        return commentRepository.findByAuthorIdAndStatus(userId, CommentStatus.PUBLISHED, PageRequest.of(from / size, size))
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));
        return toDto(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminCommentDto> getAllComments(Integer from, Integer size) {
        return commentRepository.findAll(PageRequest.of(from / size, size))
                .stream()
                .map(this::toAdminDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AdminCommentDto updateCommentStatus(Long commentId, UpdateCommentStatusDto updateCommentStatusDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));

        comment.setStatus(updateCommentStatusDto.getStatus());

        return toAdminDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));

        commentRepository.delete(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getCommentsCountByEvent(Long eventId) {
        return commentRepository.countByEventIdAndStatus(eventId, CommentStatus.PUBLISHED);
    }

    private CommentDto toDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setEvent(comment.getEvent().getId());
        dto.setCreatedOn(comment.getCreatedOn());

        UserShortDto authorDto = new UserShortDto();
        authorDto.setId(comment.getAuthor().getId());
        authorDto.setName(comment.getAuthor().getName());
        dto.setAuthor(authorDto);

        return dto;
    }

    private AdminCommentDto toAdminDto(Comment comment) {
        AdminCommentDto dto = new AdminCommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setEvent(comment.getEvent().getId());
        dto.setCreatedOn(comment.getCreatedOn());
        dto.setStatus(comment.getStatus());

        UserShortDto authorDto = new UserShortDto();
        authorDto.setId(comment.getAuthor().getId());
        authorDto.setName(comment.getAuthor().getName());
        dto.setAuthor(authorDto);

        return dto;
    }
}