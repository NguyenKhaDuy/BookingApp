package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbackRequest {
    private String id_feedback;
    private String content;
    private String customer_id;
    private String request_id;
}
