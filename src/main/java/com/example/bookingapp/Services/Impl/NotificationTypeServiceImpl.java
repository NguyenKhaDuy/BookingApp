package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.LevelEntity;
import com.example.bookingapp.Entity.NotificationTypeEntity;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.LevelDTO;
import com.example.bookingapp.Models.DTO.NotificationTypeDTO;
import com.example.bookingapp.Models.Request.NotificationTypeRequest;
import com.example.bookingapp.Models.Response.MessageResponse;
import com.example.bookingapp.Repository.NotificationTypeRepository;
import com.example.bookingapp.Services.NotificationTypeService;
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
public class NotificationTypeServiceImpl implements NotificationTypeService {
    @Autowired
    NotificationTypeRepository notificationTypeRepository;
    @Autowired
    ModelMapper modelMapper;
    @Override
    public Page<NotificationTypeDTO> getAll(Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        Page<NotificationTypeEntity> notificationTypeEntities  = notificationTypeRepository.findAll(pageable);
        List<NotificationTypeDTO> notificationTypeDTOS = new ArrayList<>();
        for (NotificationTypeEntity notificationTypeEntity : notificationTypeEntities){
            NotificationTypeDTO notificationTypeDTO = new NotificationTypeDTO();
            modelMapper.map(notificationTypeEntity, notificationTypeDTO);
            notificationTypeDTOS.add(notificationTypeDTO);
        }
        return new PageImpl<>(notificationTypeDTOS, notificationTypeEntities.getPageable(), notificationTypeEntities.getTotalElements());
    }

    @Override
    public List<NotificationTypeDTO> getAll() {
        List<NotificationTypeEntity> notificationTypeEntities  = notificationTypeRepository.findAll();
        List<NotificationTypeDTO> notificationTypeDTOS = new ArrayList<>();
        for (NotificationTypeEntity notificationTypeEntity : notificationTypeEntities){
            NotificationTypeDTO notificationTypeDTO = new NotificationTypeDTO();
            modelMapper.map(notificationTypeEntity, notificationTypeDTO);
            notificationTypeDTOS.add(notificationTypeDTO);
        }
        return notificationTypeDTOS;
    }

    @Override
    public Object detailNotifyType(Long id_type) {
        ErrorDTO errorDTO = new ErrorDTO();
        try{
            NotificationTypeEntity notificationTypeEntity = notificationTypeRepository.findById(id_type).get();
            NotificationTypeDTO notificationTypeDTO = new NotificationTypeDTO();
            modelMapper.map(notificationTypeEntity, notificationTypeDTO);
            return notificationTypeDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found notification type");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object createNotifyType(NotificationTypeRequest notificationTypeRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageResponse messageResponse = new MessageResponse();
        try {
            NotificationTypeEntity notificationTypeEntity = new NotificationTypeEntity();
            modelMapper.map(notificationTypeRequest, notificationTypeEntity);
            notificationTypeEntity.setCreated_at(LocalDateTime.now());
            notificationTypeEntity.setUpdated_at(LocalDateTime.now());
            notificationTypeRepository.save(notificationTypeEntity);
            messageResponse.setMessage("Success");
            messageResponse.setHttpStatus(HttpStatus.OK);
            return messageResponse;
        }catch (Exception ex){
            errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
            errorDTO.setMessage("Fail");
            return errorDTO;
        }
    }

    @Override
    public Object updateNotifyType(NotificationTypeRequest notificationTypeRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageResponse messageResponse = new MessageResponse();
        try {
            NotificationTypeEntity notificationTypeEntity = notificationTypeRepository.findById(notificationTypeRequest.getId()).get();
            modelMapper.map(notificationTypeRequest, notificationTypeEntity);
            notificationTypeEntity.setUpdated_at(LocalDateTime.now());
            notificationTypeRepository.save(notificationTypeEntity);
            messageResponse.setMessage("Success");
            messageResponse.setHttpStatus(HttpStatus.OK);
            return messageResponse;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found notification type");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object deleteNotifyType(Long id_type) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageResponse messageResponse = new MessageResponse();
        try {
            NotificationTypeEntity notificationTypeEntity = notificationTypeRepository.findById(id_type).get();
            notificationTypeRepository.delete(notificationTypeEntity);
            messageResponse.setMessage("Success");
            messageResponse.setHttpStatus(HttpStatus.OK);
            return messageResponse;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found notification type");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }
}
