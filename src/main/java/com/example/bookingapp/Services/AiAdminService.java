package com.example.bookingapp.Services;

import com.example.bookingapp.Models.Request.AiExampleUpsertRequest;
import com.example.bookingapp.Models.Request.AiIntentUpsertRequest;
import com.example.bookingapp.Models.Request.AiKnowledgeUpsertRequest;
import com.example.bookingapp.Models.Request.AiTestRequest;

import java.util.List;
import java.util.Map;

public interface AiAdminService {
    List<Map<String, Object>> intents();

    Map<String, Object> createIntent(AiIntentUpsertRequest request);

    Map<String, Object> updateIntent(String id, AiIntentUpsertRequest request);

    Map<String, Object> setIntentStatus(String id, boolean enabled);

    List<Map<String, Object>> examples(String intentId, Boolean verified);

    Map<String, Object> createExample(AiExampleUpsertRequest request);

    Map<String, Object> updateExample(String id, AiExampleUpsertRequest request);

    Map<String, Object> approveExample(String id);

    void deleteExample(String id);

    List<Map<String, Object>> knowledge(String intentId);

    Map<String, Object> createKnowledge(AiKnowledgeUpsertRequest request);

    Map<String, Object> updateKnowledge(String id, AiKnowledgeUpsertRequest request);

    Map<String, Object> setKnowledgeStatus(String id, boolean enabled);

    void deleteKnowledge(String id);

    Map<String, Object> test(AiTestRequest request);

    Map<String, Object> stats();
}
