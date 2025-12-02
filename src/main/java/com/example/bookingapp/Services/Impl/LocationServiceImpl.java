package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.LocationEntity;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.LocationDTO;
import com.example.bookingapp.Models.DTO.MessageDTO;
import com.example.bookingapp.Models.Request.LocationRequest;
import com.example.bookingapp.Repository.LocationRepository;
import com.example.bookingapp.Services.LocationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

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

    @Override
    public Object detailLocation(Long id_location) {
        ErrorDTO errorDTO = new ErrorDTO();
        try{
            LocationEntity locationEntity = locationRepository.findById(id_location).get();
            LocationDTO locationDTO = new LocationDTO();
            modelMapper.map(locationEntity, locationDTO);
            return locationDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found location");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object createLocation(LocationRequest locationRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageDTO messageDTO = new MessageDTO();
        try {
            LocationEntity locationEntity = new LocationEntity();
            modelMapper.map(locationRequest, locationEntity);
            locationEntity.setUpdated_at(LocalDateTime.now());
            locationRepository.save(locationEntity);
            messageDTO.setMessage("Success");
            messageDTO.setHttpStatus(HttpStatus.OK);
            return messageDTO;
        }catch (Exception ex){
            errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
            errorDTO.setMessage("Fail");
            return errorDTO;
        }
    }

    @Override
    public Object updateLocation(LocationRequest locationRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageDTO messageDTO = new MessageDTO();
        try {
            LocationEntity locationEntity = locationRepository.findById(locationRequest.getId_location()).get();
            modelMapper.map(locationRequest, locationEntity);
            locationEntity.setUpdated_at(LocalDateTime.now());
            locationRepository.save(locationEntity);
            messageDTO.setMessage("Success");
            messageDTO.setHttpStatus(HttpStatus.OK);
            return messageDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found location");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object deleteLocation(Long id_location) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageDTO messageDTO = new MessageDTO();
        try {
            LocationEntity locationEntity = locationRepository.findById(id_location).get();
            locationRepository.delete(locationEntity);
            messageDTO.setMessage("Success");
            messageDTO.setHttpStatus(HttpStatus.OK);
            return messageDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found location");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }
}
