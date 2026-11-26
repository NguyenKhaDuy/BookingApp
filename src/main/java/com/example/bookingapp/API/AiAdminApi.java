package com.example.bookingapp.API;

import com.example.bookingapp.Models.Request.*;
import com.example.bookingapp.Services.AiAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
public class AiAdminApi {
    private final AiAdminService service;

    @GetMapping("/api/admin/ai/stats")
    public Map<String, Object> stats() {
        return service.stats();
    }

    @GetMapping("/api/admin/ai/intents")
    public List<Map<String, Object>> intents() {
        return service.intents();
    }

    @PostMapping("/api/admin/ai/intents")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createIntent(@RequestBody AiIntentUpsertRequest r) {
        return service.createIntent(r);
    }

    @PutMapping("/api/admin/ai/intents/{id}")
    public Map<String, Object> updateIntent(@PathVariable String id, @RequestBody AiIntentUpsertRequest r) {
        return service.updateIntent(id, r);
    }

    @PatchMapping("/api/admin/ai/intents/{id}/status")
    public Map<String, Object> intentStatus(@PathVariable String id, @RequestParam boolean enabled) {
        return service.setIntentStatus(id, enabled);
    }

    @GetMapping("/api/admin/ai/examples")
    public List<Map<String, Object>> examples(@RequestParam(required = false) String intentId, @RequestParam(required = false) Boolean verified) {
        return service.examples(intentId, verified);
    }

    @PostMapping("/api/admin/ai/examples")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createExample(@RequestBody AiExampleUpsertRequest r) {
        return service.createExample(r);
    }

    @PutMapping("/api/admin/ai/examples/{id}")
    public Map<String, Object> updateExample(@PathVariable String id, @RequestBody AiExampleUpsertRequest r) {
        return service.updateExample(id, r);
    }

    @PatchMapping("/api/admin/ai/examples/{id}/approve")
    public Map<String, Object> approve(@PathVariable String id) {
        return service.approveExample(id);
    }

    @DeleteMapping("/api/admin/ai/examples/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExample(@PathVariable String id) {
        service.deleteExample(id);
    }

    @GetMapping("/api/admin/ai/knowledge")
    public List<Map<String, Object>> knowledge(@RequestParam(required = false) String intentId) {
        return service.knowledge(intentId);
    }

    @PostMapping("/api/admin/ai/knowledge")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createKnowledge(@RequestBody AiKnowledgeUpsertRequest r) {
        return service.createKnowledge(r);
    }

    @PutMapping("/api/admin/ai/knowledge/{id}")
    public Map<String, Object> updateKnowledge(@PathVariable String id, @RequestBody AiKnowledgeUpsertRequest r) {
        return service.updateKnowledge(id, r);
    }

    @PatchMapping("/api/admin/ai/knowledge/{id}/status")
    public Map<String, Object> knowledgeStatus(@PathVariable String id, @RequestParam boolean enabled) {
        return service.setKnowledgeStatus(id, enabled);
    }

    @DeleteMapping("/api/admin/ai/knowledge/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteKnowledge(@PathVariable String id) {
        service.deleteKnowledge(id);
    }

    @PostMapping("/api/admin/ai/test")
    public Map<String, Object> test(@RequestBody AiTestRequest r) {
        return service.test(r);
    }
}
