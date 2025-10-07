package com.example.bookingapp.Models.DTO;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RatingDTO {
    private Long id_rating;
    private Integer stars;
    private String comment;
    private String id_user;
    private String avatarBase64;
    private String full_name;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
