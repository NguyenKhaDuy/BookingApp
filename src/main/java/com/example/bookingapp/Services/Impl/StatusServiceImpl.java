package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.LocationEntity;
import com.example.bookingapp.Entity.StatusEntity;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.LocationDTO;
import com.example.bookingapp.Models.DTO.MessageDTO;
import com.example.bookingapp.Models.DTO.StatusDTO;
import com.example.bookingapp.Models.Request.StatusRequest;
import com.example.bookingapp.Repository.StatusRepository;
import com.example.bookingapp.Services.StatusService;
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
public class StatusServiceImpl implements StatusService {
    @Autowired
    StatusRepository statusRepository;
    @Autowired
    ModelMapper modelMapper;
    @Override
    public Page<StatusDTO> getAll(Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1,10);
        Page<StatusEntity> statusEntities = statusRepository.findAll(pageable);
        List<StatusDTO> statusDTOS = new ArrayList<>();
        for(StatusEntity statusEntity : statusEntities){
            StatusDTO statusDTO = new StatusDTO();
            modelMapper.map(statusEntity, statusDTO);
            statusDTOS.add(statusDTO);
        }
        return new PageImpl<>(statusDTOS, statusEntities.getPageable(), statusEntities.getTotalElements());
    }

    @Override
    public Object detailStatus(Long id_status) {
        ErrorDTO errorDTO = new ErrorDTO();
        try{
            StatusEntity statusEntity = statusRepository.findById(id_status).get();
            StatusDTO statusDTO = new StatusDTO();
            modelMapper.map(statusEntity, statusDTO);
            return statusDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found status");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object createStatus(StatusRequest statusRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageDTO messageDTO = new MessageDTO();
        try {
            StatusEntity statusEntity = new StatusEntity();
            modelMapper.map(statusRequest, statusEntity);
            statusEntity.setUpdated_at(LocalDateTime.now());
            statusRepository.save(statusEntity);
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
    public Object updateStatus(StatusRequest statusRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageDTO messageDTO = new MessageDTO();
        try {
            StatusEntity statusEntity = statusRepository.findById(statusRequest.getId_status()).get();
            modelMapper.map(statusRequest, statusEntity);
            statusEntity.setUpdated_at(LocalDateTime.now());
            statusRepository.save(statusEntity);
            messageDTO.setMessage("Success");
            messageDTO.setHttpStatus(HttpStatus.OK);
            return messageDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found status");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object deleteStatus(Long id_status) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageDTO messageDTO = new MessageDTO();
        try {
            StatusEntity statusEntity = statusRepository.findById(id_status).get();
            statusRepository.delete(statusEntity);
            messageDTO.setMessage("Success");
            messageDTO.setHttpStatus(HttpStatus.OK);
            return messageDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found status");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }
}
