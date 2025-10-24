package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.MessageDTO;
import com.example.bookingapp.Models.DTO.NotificationDTO;
import com.example.bookingapp.Models.Request.DeleteRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface NotificationUserService {
    Page<NotificationDTO> getAllByUser(String id_user, Integer pageNo);
    Object getById(String id_user, Long id_notify);
    Object deleteNotification(String id_user, DeleteRequest deleteRequest);
}
