package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReplyFeedbackRequest {
    private String body;
    private Long id_feedback;
}
