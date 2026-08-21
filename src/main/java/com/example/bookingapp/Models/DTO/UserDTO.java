package com.example.bookingapp.Models.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserDTO {
    private String id_user;
    private String full_name;
    private String address;
    private String phone_number;
    private String email;
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate dob;
    private String gender;
    private List<String> role_name = new ArrayList<>();
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime created_at;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime updated_at;
}
