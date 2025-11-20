package com.example.bookingapp.Models.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RequestFeedbackDTO {
    private Long id_request;
    private String description;
    private String name_techinician; //tự set
    private String id_technician; //tự set
    private String name_service; // tự set
    private String name_customer; //tự set
    private String phone_number_customer; //tự set
    private String email_customer; //tự set
    private List<FeedbackDTO> feedbackDTOS = new ArrayList<>();
}
