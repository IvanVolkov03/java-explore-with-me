package ru.practicum.main.comment.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCommentDto {
    @Size(min = 10, max = 1000)
    private String text;
}