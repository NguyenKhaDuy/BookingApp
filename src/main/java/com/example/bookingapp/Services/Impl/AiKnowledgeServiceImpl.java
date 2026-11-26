
package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.AiKnowledgeEntity;
import com.example.bookingapp.Repository.AiKnowledgeRepository;
import com.example.bookingapp.Services.AiKnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiKnowledgeServiceImpl implements AiKnowledgeService {
    private final AiKnowledgeRepository repository;

    @Override
    @Transactional(readOnly = true)
    public String getKnowledge(String intentCode) {
        StringBuilder result = new StringBuilder();
        for (AiKnowledgeEntity item : repository.findByIntent_CodeAndEnabledTrue(intentCode)) {
            if (item.getSystemPrompt() != null && !item.getSystemPrompt().isBlank())
                result.append(item.getSystemPrompt()).append("\n");
            if (item.getContent() != null && !item.getContent().isBlank())
                result.append("Kiến thức tham khảo: ").append(item.getContent()).append("\n");
        }
        return result.toString();
    }
}
