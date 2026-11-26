
package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.AiIntentEntity;
import com.example.bookingapp.Entity.AiIntentExampleEntity;
import com.example.bookingapp.Models.DTO.AiClassificationResult;
import com.example.bookingapp.Repository.AiIntentRepository;
import com.example.bookingapp.Repository.AiIntentExampleRepository;
import com.example.bookingapp.Services.AiIntentLearningService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiIntentLearningServiceImpl
        implements AiIntentLearningService {

    private final AiIntentRepository intentRepository;
    private final AiIntentExampleRepository exampleRepository;

    @Override
    @Transactional
    public void learn(
            String question,
            AiClassificationResult classification
    ) {
        if (question == null || question.isBlank()
                || question.length() > 1000
                || classification == null) {
            return;
        }

        if (!"AI".equals(classification.getSource())
                || "OTHER".equals(classification.getIntentCode())
                || classification.getConfidence() < 0.85) {
            return;
        }

        AiIntentEntity intent = intentRepository
                .findByCode(classification.getIntentCode())
                .orElse(null);

        if (intent == null
                || !Boolean.TRUE.equals(intent.getEnabled())) {
            return;
        }

        String text = question.trim();

        boolean exists = exampleRepository
                .existsByIntentIdAndExampleTextIgnoreCase(
                        intent.getId(), text);

        if (exists) {
            return;
        }

        AiIntentExampleEntity example =
                new AiIntentExampleEntity();

        example.setExampleText(text);
        example.setVerified(false);
        example.setIntent(intent);

        exampleRepository.save(example);
    }

    @Override
    @Transactional
    public void approve(String exampleId) {

        AiIntentExampleEntity example =
                exampleRepository.findById(exampleId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy câu hỏi mẫu"));

        example.setVerified(true);
        exampleRepository.save(example);
    }
}
