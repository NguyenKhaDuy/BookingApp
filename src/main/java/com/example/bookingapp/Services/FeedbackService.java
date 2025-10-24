package com.example.bookingapp.Services;

import com.example.bookingapp.Models.Request.FeedbackRequest;
import org.springframework.stereotype.Service;

@Service
public interface FeedbackService {
    Object createFeedback(FeedbackRequest feedbackRequest);
}
