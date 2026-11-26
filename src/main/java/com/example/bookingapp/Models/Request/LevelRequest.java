package com.example.bookingapp.Models.Request;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LevelRequest {
    private String id_level;
    private String level;
    private String description;
}
