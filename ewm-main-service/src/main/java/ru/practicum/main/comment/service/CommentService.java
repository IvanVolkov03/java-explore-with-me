package ru.practicum.main.comment.service;

import ru.practicum.main.comment.dto.*;

import java.util.List;

public interface CommentService {
    CommentDto addComment(Long userId, Long eventId, NewCommentDto newCommentDto);

    CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto updateCommentDto);

    void deleteComment(Long userId, Long commentId);

    List<CommentDto> getCommentsByEvent(Long eventId, Integer from, Integer size);

    List<CommentDto> getCommentsByUser(Long userId, Integer from, Integer size);

    CommentDto getCommentById(Long commentId);

    List<AdminCommentDto> getAllComments(Integer from, Integer size);

    AdminCommentDto updateCommentStatus(Long commentId, UpdateCommentStatusDto updateCommentStatusDto);

    void deleteCommentByAdmin(Long commentId);

    Long getCommentsCountByEvent(Long eventId);
}