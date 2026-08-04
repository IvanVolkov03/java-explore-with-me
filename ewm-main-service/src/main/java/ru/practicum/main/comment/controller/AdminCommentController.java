package ru.practicum.main.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.comment.dto.AdminCommentDto;
import ru.practicum.main.comment.dto.UpdateCommentStatusDto;
import ru.practicum.main.comment.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentService commentService;

    @GetMapping
    public List<AdminCommentDto> getAllComments(@RequestParam(defaultValue = "0") Integer from,
                                                @RequestParam(defaultValue = "10") Integer size) {
        return commentService.getAllComments(from, size);
    }

    @PatchMapping("/{commentId}")
    public AdminCommentDto updateCommentStatus(@PathVariable Long commentId,
                                               @Valid @RequestBody UpdateCommentStatusDto updateCommentStatusDto) {
        return commentService.updateCommentStatus(commentId, updateCommentStatusDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {
        commentService.deleteCommentByAdmin(commentId);
    }
}