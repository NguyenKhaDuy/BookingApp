package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class ProfileRequest {
    private String id_user;
    private String full_name;
    private String address;
    private String phone_number;
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate dob;
    private String gender;
}
