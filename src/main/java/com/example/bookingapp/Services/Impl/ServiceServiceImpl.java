package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.NotificationsEntity;
import com.example.bookingapp.Entity.ServiceEntity;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.MessageDTO;
import com.example.bookingapp.Models.DTO.ServiceDTO;
import com.example.bookingapp.Models.Request.ServiceRequest;
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

import java.time.LocalDateTime;
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

    @Override
    public Object createService(ServiceRequest serviceRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageDTO messageDTO = new MessageDTO();
        try {
            ServiceEntity serviceEntity = new ServiceEntity();
            modelMapper.map(serviceRequest, serviceEntity);
            serviceEntity.setCreated_at(LocalDateTime.now());
            serviceEntity.setUpdated_at(LocalDateTime.now());
            serviceRepository.save(serviceEntity);
            messageDTO.setMessage("Success");
            messageDTO.setHttpStatus(HttpStatus.OK);
            return messageDTO;
        }catch (Exception ex){
            errorDTO.setMessage("Fail");
            errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
            return errorDTO;
        }
    }

    @Override
    public Object updateService(ServiceRequest serviceRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageDTO messageDTO = new MessageDTO();
        try {
            ServiceEntity serviceEntity = serviceRepository.findById(serviceRequest.getId_service()).get();
            modelMapper.map(serviceRequest, serviceEntity);
            serviceEntity.setUpdated_at(LocalDateTime.now());
            serviceRepository.save(serviceEntity);
            messageDTO.setMessage("Success");
            messageDTO.setHttpStatus(HttpStatus.OK);
            return messageDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found service");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object deleteService(Long id_service) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageDTO messageDTO = new MessageDTO();
        try {
            ServiceEntity serviceEntity = serviceRepository.findById(id_service).get();
            serviceRepository.delete(serviceEntity);
            messageDTO.setMessage("Success");
            messageDTO.setHttpStatus(HttpStatus.OK);
            return messageDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found service");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }
}
