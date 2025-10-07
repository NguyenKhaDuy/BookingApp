package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.RatingEntity;
import com.example.bookingapp.Entity.TechnicianEntity;
import com.example.bookingapp.Models.DTO.RatingDTO;
import com.example.bookingapp.Repository.RatingRepository;
import com.example.bookingapp.Repository.TechnicianRepository;
import com.example.bookingapp.Services.TechnicianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TechnicianServiceImpl implements TechnicianService {
    @Autowired
    RatingRepository ratingRepository;

    @Autowired
    TechnicianRepository technicianRepository;
    @Override
    public Page<RatingDTO> getAllRatingsByTechnician(Integer pageNo, String id_technician) {
        Pageable pageable = PageRequest.of(pageNo - 1, 10);

        TechnicianEntity technicianEntity = technicianRepository.findById(id_technician);

        Page<RatingEntity> ratingEntities = ratingRepository.findByTechnicianEntity();
        return null;
    }
}
