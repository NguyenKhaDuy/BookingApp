package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.LocationDTO;
import com.example.bookingapp.Models.Request.LocationRequest;
import com.example.bookingapp.Models.Request.SkillRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface LocationService {
    Page<LocationDTO> getAll(Integer pageNo);
    Object detailLocation(Long id_location);
    Object createLocation(LocationRequest locationRequest);
    Object updateLocation(LocationRequest locationRequest);
    Object deleteLocation(Long id_location);
}
