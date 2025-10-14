package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.LocationDTO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface LocationService {
    Page<LocationDTO> getAll(Integer pageNo);
}
