package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.AiClassificationResult;
import org.springframework.stereotype.Service;

@Service
public interface AiIntentLearningService {
    void learn(
            String question,
            AiClassificationResult classification
    );

    void approve(String exampleId);
}
