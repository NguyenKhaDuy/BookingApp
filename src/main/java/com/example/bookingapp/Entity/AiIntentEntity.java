package com.example.bookingapp.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ai_intent")
public class AiIntentEntity {
    @Id
    @Column(name = "id")
    private String id;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean enabled = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(
            mappedBy = "intent",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<AiIntentExampleEntity> examples =
            new ArrayList<>();

    @OneToMany(
            mappedBy = "intent",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<AiKnowledgeEntity> knowledge =
            new ArrayList<>();
}
