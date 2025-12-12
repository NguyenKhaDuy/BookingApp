package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class test {
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate date;
    private LocalTime time;
    private Long id_service;
}
