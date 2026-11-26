package com.example.bookingapp.Services;

import com.example.bookingapp.Entity.ConversationEntity;

public interface AiConversationContextService {
    String getContext(ConversationEntity conversation);

    void saveContext(
            ConversationEntity conversation,
            String intentCode,
            String question
    );

    void deleteContext(String conversationId);
}
