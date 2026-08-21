package com.example.bookingapp.Models.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RevenueTechnicianDTO {
    private Long year;
    private List<RevenueDTO> revenueDTOS;
}
