package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.*;
import com.example.bookingapp.Models.DTO.AiClassificationResult;
import com.example.bookingapp.Models.Request.AiExampleUpsertRequest;
import com.example.bookingapp.Models.Request.AiIntentUpsertRequest;
import com.example.bookingapp.Models.Request.AiKnowledgeUpsertRequest;
import com.example.bookingapp.Models.Request.AiTestRequest;
import com.example.bookingapp.Repository.*;
import com.example.bookingapp.Services.AiAdminService;
import com.example.bookingapp.Services.DynamicIntentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AiAdminServiceImpl implements AiAdminService {
    private final AiIntentRepository intentRepository;
    private final AiIntentExampleRepository exampleRepository;
    private final AiKnowledgeRepository knowledgeRepository;
    private final DynamicIntentService dynamicIntentService;

    private static String required(String value, String field) {
        if (value == null || value.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " không được trống");
        return value.trim();
    }

    private AiIntentEntity intent(String id) {
        if (id == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu intentId");
        return intentRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy intent"));
    }

    private AiIntentExampleEntity example(String id) {
        return exampleRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy example"));
    }

    private AiKnowledgeEntity knowledgeEntity(String id) {
        return knowledgeRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy knowledge"));
    }

    private Map<String, Object> intentView(AiIntentEntity e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("code", e.getCode());
        m.put("name", e.getName());
        m.put("description", e.getDescription());
        m.put("enabled", e.getEnabled());
        return m;
    }

    private Map<String, Object> exampleView(AiIntentExampleEntity e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("intentId", e.getIntent().getId());
        m.put("intentCode", e.getIntent().getCode());
        m.put("exampleText", e.getExampleText());
        m.put("verified", e.getVerified());
        return m;
    }

    private Map<String, Object> knowledgeView(AiKnowledgeEntity e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("intentId", e.getIntent().getId());
        m.put("intentCode", e.getIntent().getCode());
        m.put("title", e.getTitle());
        m.put("content", e.getContent());
        m.put("systemPrompt", e.getSystemPrompt());
        m.put("enabled", e.getEnabled());
        return m;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> intents() {
        return intentRepository.findAll().stream().sorted(Comparator.comparing(AiIntentEntity::getId)).map(this::intentView).toList();
    }

    @Override
    @Transactional
    public Map<String, Object> createIntent(AiIntentUpsertRequest r) {
        String code = required(r.getCode(), "Mã intent").toUpperCase(Locale.ROOT);
        if (!code.matches("[A-Z][A-Z0-9_]{1,99}"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã chỉ gồm A-Z, 0-9, _");
        if (intentRepository.findByCode(code).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã intent đã tồn tại");
        AiIntentEntity e = new AiIntentEntity();
        e.setCode(code);
        e.setName(required(r.getName(), "Tên intent"));
        e.setDescription(r.getDescription());
        e.setEnabled(r.getEnabled() == null || r.getEnabled());
        e.setCreatedAt(LocalDateTime.now());
        return intentView(intentRepository.save(e));
    }

    @Override
    @Transactional
    public Map<String, Object> updateIntent(String id, AiIntentUpsertRequest r) {
        AiIntentEntity e = intent(id);
        // code được giữ cố định: đổi code có thể phá các handler nghiệp vụ đang dùng trong ChatServiceImpl
        if (r.getCode() != null && !e.getCode().equals(r.getCode().trim().toUpperCase(Locale.ROOT)))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể đổi mã Intent. Hãy tạo Intent mới.");
        e.setName(required(r.getName(), "Tên intent"));
        e.setDescription(r.getDescription());
        if (r.getEnabled() != null) e.setEnabled(r.getEnabled());
        return intentView(intentRepository.save(e));
    }

    @Override
    @Transactional
    public Map<String, Object> setIntentStatus(String id, boolean enabled) {
        AiIntentEntity e = intent(id);
        e.setEnabled(enabled);
        return intentView(intentRepository.save(e));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> examples(String intentId, Boolean verified) {
        return exampleRepository.findAll().stream()
                .filter(e -> intentId == null || e.getIntent().getId().equals(intentId))
                .filter(e -> verified == null || verified.equals(e.getVerified()))
                .sorted(Comparator.comparing(AiIntentExampleEntity::getId).reversed()).map(this::exampleView).toList();
    }

    private void checkDuplicate(String intentId, String text, String exceptId) {
        boolean duplicate = exampleRepository.findAll().stream().anyMatch(e -> e.getIntent().getId().equals(intentId)
                && e.getExampleText().equalsIgnoreCase(text) && !e.getId().equals(exceptId));
        if (duplicate) throw new ResponseStatusException(HttpStatus.CONFLICT, "Câu mẫu đã tồn tại trong intent này");
    }

    @Override
    @Transactional
    public Map<String, Object> createExample(AiExampleUpsertRequest r) {
        AiIntentEntity i = intent(r.getIntentId());
        String text = required(r.getExampleText(), "Câu mẫu");
        checkDuplicate(i.getId(), text, null);
        AiIntentExampleEntity e = new AiIntentExampleEntity();
        e.setIntent(i);
        e.setExampleText(text);
        e.setVerified(Boolean.TRUE.equals(r.getVerified()));
        e.setCreatedAt(LocalDateTime.now());
        return exampleView(exampleRepository.save(e));
    }

    @Override
    @Transactional
    public Map<String, Object> updateExample(String id, AiExampleUpsertRequest r) {
        AiIntentExampleEntity e = example(id);
        AiIntentEntity i = intent(r.getIntentId());
        String text = required(r.getExampleText(), "Câu mẫu");
        checkDuplicate(i.getId(), text, id);
        e.setIntent(i);
        e.setExampleText(text);
        if (r.getVerified() != null) e.setVerified(r.getVerified());
        return exampleView(exampleRepository.save(e));
    }

    @Override
    @Transactional
    public Map<String, Object> approveExample(String id) {
        AiIntentExampleEntity e = example(id);
        e.setVerified(true);
        return exampleView(exampleRepository.save(e));
    }

    @Override
    @Transactional
    public void deleteExample(String id) {
        exampleRepository.delete(example(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> knowledge(String intentId) {
        return knowledgeRepository.findAll().stream().filter(e -> intentId == null || e.getIntent().getId().equals(intentId))
                .sorted(Comparator.comparing(AiKnowledgeEntity::getId).reversed()).map(this::knowledgeView).toList();
    }

    @Override
    @Transactional
    public Map<String, Object> createKnowledge(AiKnowledgeUpsertRequest r) {
        AiKnowledgeEntity e = new AiKnowledgeEntity();
        e.setIntent(intent(r.getIntentId()));
        e.setTitle(required(r.getTitle(), "Tiêu đề"));
        e.setContent(r.getContent());
        e.setSystemPrompt(r.getSystemPrompt());
        e.setEnabled(r.getEnabled() == null || r.getEnabled());
        e.setUpdatedAt(LocalDateTime.now());
        return knowledgeView(knowledgeRepository.save(e));
    }

    @Override
    @Transactional
    public Map<String, Object> updateKnowledge(String id, AiKnowledgeUpsertRequest r) {
        AiKnowledgeEntity e = knowledgeEntity(id);
        e.setIntent(intent(r.getIntentId()));
        e.setTitle(required(r.getTitle(), "Tiêu đề"));
        e.setContent(r.getContent());
        e.setSystemPrompt(r.getSystemPrompt());
        if (r.getEnabled() != null) e.setEnabled(r.getEnabled());
        e.setUpdatedAt(LocalDateTime.now());
        return knowledgeView(knowledgeRepository.save(e));
    }

    @Override
    @Transactional
    public Map<String, Object> setKnowledgeStatus(String id, boolean enabled) {
        AiKnowledgeEntity e = knowledgeEntity(id);
        e.setEnabled(enabled);
        e.setUpdatedAt(LocalDateTime.now());
        return knowledgeView(knowledgeRepository.save(e));
    }

    @Override
    @Transactional
    public void deleteKnowledge(String id) {
        knowledgeRepository.delete(knowledgeEntity(id));
    }

    @Override
    public Map<String, Object> test(AiTestRequest r) {
        String message = required(r.getMessage(), "Câu hỏi");
        AiClassificationResult c = dynamicIntentService.classify(message, r.getContext() == null ? "{}" : r.getContext());
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("intentCode", c == null ? "OTHER" : c.getIntentCode());
        m.put("confidence", c == null ? 0 : c.getConfidence());
        m.put("source", c == null ? "NONE" : c.getSource());
        return m;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> stats() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalIntents", intentRepository.count());
        m.put("activeIntents", intentRepository.findAll().stream().filter(e -> Boolean.TRUE.equals(e.getEnabled())).count());
        m.put("pendingExamples", exampleRepository.findAll().stream().filter(e -> !Boolean.TRUE.equals(e.getVerified())).count());
        m.put("totalKnowledge", knowledgeRepository.count());
        return m;
    }
}
