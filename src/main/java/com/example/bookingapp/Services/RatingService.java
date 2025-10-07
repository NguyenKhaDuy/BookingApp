package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.RatingDTO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TechnicianService {
    Page<RatingDTO> getAllRatingsByTechnician(Integer pageNo, String id_technician);
}
