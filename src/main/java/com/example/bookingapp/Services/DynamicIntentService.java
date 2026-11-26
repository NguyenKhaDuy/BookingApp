package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.AiClassificationResult;
import org.springframework.stereotype.Service;

@Service
public interface DynamicIntentService {
    AiClassificationResult classify(
            String question,
            String context
    );
}
