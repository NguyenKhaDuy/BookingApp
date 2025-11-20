package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.NotificationsEntity;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.MessageDTO;
import com.example.bookingapp.Models.DTO.NotificationDTO;
import com.example.bookingapp.Models.Request.NotificationRequest;
import com.example.bookingapp.Repository.NotificationRepository;
import com.example.bookingapp.Services.NotificationService;
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
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    ModelMapper modelMapper;
    @Override
    public Page<NotificationDTO> getAll(Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        Page<NotificationsEntity> notificationsEntities = notificationRepository.findAll(pageable);
        List<NotificationDTO> notificationDTOS = new ArrayList<>();
        for (NotificationsEntity notificationsEntity : notificationsEntities){
            NotificationDTO notificationDTO = new NotificationDTO();
            modelMapper.map(notificationsEntity, notificationDTO);
            notificationDTOS.add(notificationDTO);
        }
        return new PageImpl<>(notificationDTOS, notificationsEntities.getPageable(), notificationsEntities.getTotalElements());
    }

    @Override
    public Object detailNotification(Long id_notify) {
        ErrorDTO errorDTO = new ErrorDTO();
        try {
            NotificationsEntity notificationsEntity = notificationRepository.findById(id_notify).get();
            NotificationDTO notificationDTO = new NotificationDTO();
            modelMapper.map(notificationsEntity, notificationDTO);
            return notificationDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found notify");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object deleteNotification(Long id_notify) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageDTO messageDTO = new MessageDTO();
        try {
            NotificationsEntity notificationsEntity = notificationRepository.findById(id_notify).get();
            notificationRepository.delete(notificationsEntity);
            messageDTO.setMessage("Success");
            messageDTO.setHttpStatus(HttpStatus.OK);
            return messageDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found notify");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object createNotification(NotificationRequest notificationRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageDTO messageDTO = new MessageDTO();
        try {
            NotificationsEntity notificationsEntity = new NotificationsEntity();
            modelMapper.map(notificationRequest, notificationsEntity);
            notificationsEntity.setCreated_at(LocalDateTime.now());
            notificationsEntity.setUpdated_at(LocalDateTime.now());
            notificationRepository.save(notificationsEntity);
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
    public Object updateNotification(NotificationRequest notificationRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageDTO messageDTO = new MessageDTO();
        try {
            NotificationsEntity notificationsEntity = notificationRepository.findById(notificationRequest.getId_notify()).get();
            modelMapper.map(notificationRequest, notificationsEntity);
            notificationsEntity.setUpdated_at(LocalDateTime.now());
            notificationRepository.save(notificationsEntity);
            messageDTO.setMessage("Success");
            messageDTO.setHttpStatus(HttpStatus.OK);
            return messageDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found notification");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }
}
