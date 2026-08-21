package com.example.bookingapp.Models.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StatisticRequestTechnicianDTO {
    private Long year;
    List<StatisticTechnicianDTO> statisticTechnicianDTOS;
}
