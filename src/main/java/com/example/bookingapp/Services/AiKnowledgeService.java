package com.example.bookingapp.Services;

import org.springframework.stereotype.Service;

@Service
public interface AiKnowledgeService {
    String getKnowledge(String intentCode);
}