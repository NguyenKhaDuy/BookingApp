package com.example.bookingapp.Models.DTO;

import com.example.bookingapp.Entity.LevelEntity;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TechnicicanDTO {
    private String id_user;
    private String full_name;
    private String address;
    private String phone_number;
    private String email;
    private String avatarBase64;
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate dob;
    private String gender;
    private String working_area;
    private Integer experience_year;
    private String level;
    private List<RoleDTO> roleDTOS = new ArrayList<>();
    private List<String> nameServiceTechnician = new ArrayList<>();
    private List<String> nameSkillTechnician = new ArrayList<>();
    private List<LocationTechnicianDTO> locationTechnicianDTOS = new ArrayList<>();
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime created_at;
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime updated_at;
}
