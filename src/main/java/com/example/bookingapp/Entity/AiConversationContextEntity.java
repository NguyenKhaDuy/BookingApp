package com.example.bookingapp.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ai_conversation_context")
public class AiConversationContextEntity {
    @Id
    @Column(name = "id")
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "conversation_id",
            nullable = false,
            unique = true
    )
    private ConversationEntity conversationEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_intent_id")
    private AiIntentEntity currentIntent;

    @Column(columnDefinition = "LONGTEXT")
    private String contextJson;

    private LocalDateTime updatedAt = LocalDateTime.now();
}
