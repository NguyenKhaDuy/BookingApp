package com.example.bookingapp.Models.DTO;

import com.example.bookingapp.Entity.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RepairRequestDTO {
    private Long id_request;
    private String description;
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate scheduled_date;
    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime scheduled_time;
    private String location;
    private CustomerDTO customer; // tự set
    private String name_techinician; // tự set
    private String id_technician; // tự set
    private String name_service; // tự set
    private List<String> image_request = new ArrayList<>(); //tự set
    private String status_code; //tự set
    private InvoicesDTO invoices; //tự set
    private List<FeedbackDTO> feedback = new ArrayList<>(); //tự set
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime created_at;
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime updated_at;
}
