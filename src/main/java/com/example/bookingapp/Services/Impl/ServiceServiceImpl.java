package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.ServiceEntity;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.ServiceDTO;
import com.example.bookingapp.Repository.ServiceRepository;
import com.example.bookingapp.Services.ServiceService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ServiceServiceImpl implements ServiceService {
    @Autowired
    ServiceRepository serviceRepository;
    @Autowired
    ModelMapper modelMapper;
    @Override
    public Page<ServiceDTO> getAll(Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        Page<ServiceEntity> serviceEntities = serviceRepository.findAll(pageable);
        List<ServiceDTO> serviceDTOS = new ArrayList<>();
        for(ServiceEntity serviceEntity : serviceEntities){
            ServiceDTO serviceDTO = new ServiceDTO();
            modelMapper.map(serviceEntity, serviceDTO);
            serviceDTOS.add(serviceDTO);
        }
        return new PageImpl<>(serviceDTOS, serviceEntities.getPageable(), serviceEntities.getTotalElements());
    }

    @Override
    public Object getById(Long id_service) {
        ServiceDTO serviceDTO = new ServiceDTO();
        try{
            ServiceEntity serviceEntity = serviceRepository.findById(id_service).get();
            modelMapper.map(serviceEntity, serviceDTO);
        }catch (NoSuchElementException ex){
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setMessage(ex.getMessage());
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
        return serviceDTO;
    }
}
