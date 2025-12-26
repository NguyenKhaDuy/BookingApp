package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RegisterCustomerRequest {
    private String full_name;
    private String address;
    private String phone_number;
    private String email;
    private String password;
    private LocalDate dob;
    private String gender;
}
