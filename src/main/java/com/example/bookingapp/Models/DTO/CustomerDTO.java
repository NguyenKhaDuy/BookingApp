package com.example.bookingapp.Models.DTO;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CustomerDTO {
    private String id_user;
    private String full_name;
    private String address;
    private String phone_number;
    private String email;
    private String avatarBase64;
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate dob;
    private String gender;
    private List<RoleDTO> roleDTOS = new ArrayList<>();
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime created_at;
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime updated_at;
}
