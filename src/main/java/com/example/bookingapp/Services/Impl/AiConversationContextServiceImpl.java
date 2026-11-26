
package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.*;
import com.example.bookingapp.Repository.*;
import com.example.bookingapp.Services.AiConversationContextService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AiConversationContextServiceImpl
        implements AiConversationContextService {

    private final AiConversationContextRepository contextRepository;
    private final AiIntentRepository intentRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public String getContext(
            ConversationEntity conversation
    ) {
        if (conversation == null) {
            return "{}";
        }

        return contextRepository
                .findByConversationEntity_IdConversation(
                        conversation.getIdConversation())
                .map(AiConversationContextEntity::getContextJson)
                .filter(value -> !value.isBlank())
                .orElse("{}");
    }

    @Override
    @Transactional
    public void saveContext(
            ConversationEntity conversation,
            String intentCode,
            String question
    ) {
        if (conversation == null) {
            return;
        }

        AiConversationContextEntity context =
                contextRepository
                        .findByConversationEntity_IdConversation(
                                conversation.getIdConversation())
                        .orElseGet(() -> {
                            AiConversationContextEntity entity =
                                    new AiConversationContextEntity();

                            entity.setConversationEntity(conversation);
                            return entity;
                        });

        if (intentCode != null) {
            intentRepository.findByCode(intentCode)
                    .ifPresent(context::setCurrentIntent);
        }

        try {
            ObjectNode data;

            String oldJson = context.getContextJson();

            if (oldJson == null || oldJson.isBlank()) {
                data = objectMapper.createObjectNode();
            } else {
                JsonNode parsed = objectMapper.readTree(oldJson);

                data = parsed.isObject()
                        ? (ObjectNode) parsed
                        : objectMapper.createObjectNode();
            }

            data.put("lastIntent", intentCode);
            data.put("lastQuestion", question);

            context.setContextJson(
                    objectMapper.writeValueAsString(data));

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Không thể lưu Context AI", ex);
        }

        context.setUpdatedAt(LocalDateTime.now());
        contextRepository.save(context);
    }

    @Override
    @Transactional
    public void deleteContext(String conversationId) {

        if (conversationId == null
                || conversationId.isBlank()) {
            return;
        }

        contextRepository
                .deleteByConversationEntity_IdConversation(conversationId);
    }
}
