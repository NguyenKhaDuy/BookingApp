package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class TechnicianScheduleRequest {
    private Long id_schedule;
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate date;
    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime time_start;
    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime time_end;
    private String user_id;
    //set trạng thái và ngày tháng created, updated
}
