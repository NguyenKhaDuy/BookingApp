package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.NotificationDTO;
import com.example.bookingapp.Models.Request.NotificationRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface NotificationService {
    Page<NotificationDTO> getAll(Integer pageNo);
    Object detailNotification(Long id_notify);
    Object deleteNotification(Long id_notify);
    Object createNotification(NotificationRequest notificationRequest);
    Object updateNotification(NotificationRequest notificationRequest);
}
