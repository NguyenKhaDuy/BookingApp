package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class FillterRequest {
    private String name_service;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date_from;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date_to;
    private String name_technician;
}
