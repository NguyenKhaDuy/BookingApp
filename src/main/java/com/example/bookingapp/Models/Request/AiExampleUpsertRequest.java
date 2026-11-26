package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AiExampleUpsertRequest {
    private String intentId;
    private String exampleText;
    private Boolean verified;
}
