package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.LocationDTO;
import com.example.bookingapp.Models.Request.LocationRequest;
import com.example.bookingapp.Models.Request.SkillRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface LocationService {
    Page<LocationDTO> getAll(Integer pageNo);
    List<LocationDTO> getAll();
    Object detailLocation(Long id_location);
    Object createLocation(LocationRequest locationRequest);
    Object updateLocation(LocationRequest locationRequest);
    Object deleteLocation(Long id_location);
}
