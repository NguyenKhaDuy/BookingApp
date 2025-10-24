package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbackRequest {
    private Long id_feedback;
    private String content;
    private String customer_id;
    private Long request_id;
}
