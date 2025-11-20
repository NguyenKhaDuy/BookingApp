package com.example.bookingapp.Utils;

import com.example.bookingapp.Entity.TechnicianEntity;
import com.example.bookingapp.Models.DTO.TechnicicanDTO;

public class ConvertEntityToDTO {
    public static TechnicicanDTO ToTechnicianDTO(TechnicianEntity technicianEntity){
        TechnicicanDTO technicicanDTO = new TechnicicanDTO();
        technicicanDTO.setId_user((technicianEntity.getId_user()));
        technicicanDTO.setFull_name(technicianEntity.getFull_name());
        technicicanDTO.setAddress(technicianEntity.getAddress());
        technicicanDTO.setPhone_number(technicianEntity.getPhone_number());
        technicicanDTO.setEmail(technicianEntity.getEmail());
        technicicanDTO.setDob(technicianEntity.getDob());
        technicicanDTO.setGender(technicianEntity.getGender());
        technicicanDTO.setExperience_year(technicianEntity.getExperience_year());
        technicicanDTO.setWorking_area(technicianEntity.getWorking_area());
        technicicanDTO.setTechnician_debt(technicianEntity.getTechnician_debt());
        technicicanDTO.setCreated_at(technicianEntity.getCreated_at());
        technicicanDTO.setUpdated_at(technicianEntity.getUpdated_at());
        return technicicanDTO;
    }
}
