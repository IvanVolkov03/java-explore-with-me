package ru.practicum.main.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.main.comment.model.CommentStatus;
import ru.practicum.main.event.dto.UserShortDto;

import java.time.LocalDateTime;

@Getter
@Setter
public class AdminCommentDto {
    private Long id;
    private String text;
    private Long event;
    private UserShortDto author;
    private CommentStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;
}