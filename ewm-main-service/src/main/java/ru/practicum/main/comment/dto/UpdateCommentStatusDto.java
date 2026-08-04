package ru.practicum.main.comment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.main.comment.model.CommentStatus;

@Getter
@Setter
public class UpdateCommentStatusDto {
    @NotNull
    private CommentStatus status;
}