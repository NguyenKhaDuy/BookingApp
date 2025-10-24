package com.example.bookingapp.Models.Request;

import com.example.bookingapp.Entity.LevelEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TechnicianProfileRequest extends ProfileRequest{
    private String working_area;
    private Integer experience_year;
}
