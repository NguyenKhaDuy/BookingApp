package com.example.bookingapp.Models.DTO;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class DetailFeedbackDTO {
    private Long id_request;
    private String description;
    private String name_techinician; //tự set
    private String id_technician; //tự set
    private String name_service; // tự set
    private String name_customer; //tự set
    private String phone_number_customer; //tự set
    private String email_customer; //tự set
    private Long id_feedback;
    private String content;
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime created_at;
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime updated_at;
}
