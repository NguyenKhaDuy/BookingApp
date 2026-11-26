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
@Table(name = "ai_intent_example")
public class AiIntentExampleEntity {
    @Id
    @Column(name = "id")
    private String id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String exampleText;

    @Column(nullable = false)
    private Boolean verified = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intent_id", nullable = false)
    private AiIntentEntity intent;
}
