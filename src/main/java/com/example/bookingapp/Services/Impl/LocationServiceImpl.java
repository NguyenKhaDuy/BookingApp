package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.LocationEntity;
import com.example.bookingapp.Models.DTO.LocationDTO;
import com.example.bookingapp.Repository.LocationRepository;
import com.example.bookingapp.Services.LocationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LocationServiceImpl implements LocationService {
    @Autowired
    LocationRepository locationRepository;
    @Autowired
    ModelMapper modelMapper;
    @Override
    public Page<LocationDTO> getAll(Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1,10);
        Page<LocationEntity> locationEntities = locationRepository.findAll(pageable);
        List<LocationDTO> locationDTOS = new ArrayList<>();
        for(LocationEntity locationEntity : locationEntities){
            LocationDTO locationDTO = new LocationDTO();
            modelMapper.map(locationEntity, locationDTO);
            locationDTOS.add(locationDTO);
        }
        return new PageImpl<>(locationDTOS, locationEntities.getPageable(), locationEntities.getTotalElements());
    }
}
