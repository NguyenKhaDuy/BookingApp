package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AiIntentUpsertRequest {
    private String code;
    private String name;
    private String description;
    private Boolean enabled;
}
