package com.example.bookingapp.Services;

import com.example.bookingapp.Entity.TechnicianEntity;
import com.example.bookingapp.Models.DTO.TechnicicanDTO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface TechnicianService {
    Page<TechnicicanDTO> getAll(Integer pageNo);
}
