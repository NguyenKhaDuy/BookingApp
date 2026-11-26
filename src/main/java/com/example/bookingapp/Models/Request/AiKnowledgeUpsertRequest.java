package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AiKnowledgeUpsertRequest {
    private String intentId;
    private String title;
    private String content;
    private String systemPrompt;
    private Boolean enabled;
}
