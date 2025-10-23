package com.example.bookingapp.Models.Request;

import com.example.bookingapp.Entity.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RequestCustomerRequest {
    private Long id_request;
    private String description;
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate scheduled_date;
    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime scheduled_time;
    private String location;
    private String id_customer;
    private String id_technician; //có thể để null và thợ sẽ nhận hoặc yêu cầu 1 thợ cụ thể
    private Long id_service;
    private List<MultipartFile> imageRequest = new ArrayList<>();
    //set trang thai ban dau cho yeu cau
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime created_at;
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime updated_at;
}
