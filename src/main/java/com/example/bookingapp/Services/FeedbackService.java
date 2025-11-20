package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.RequestFeedbackDTO;
import com.example.bookingapp.Models.Request.FeedbackRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface FeedbackService {
    Object createFeedback(FeedbackRequest feedbackRequest);
    Page<RequestFeedbackDTO> getAll(Integer pageNo);
    Object detailFeedback (Long id);
}
