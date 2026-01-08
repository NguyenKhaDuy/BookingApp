package com.example.bookingapp.Services;

import com.example.bookingapp.Entity.StatusEntity;
import com.example.bookingapp.Entity.UserEntity;
import com.example.bookingapp.Models.DTO.MessageNotifiDTO;
import com.example.bookingapp.Models.DTO.NotificationDTO;
import com.example.bookingapp.Models.Request.DeleteRequest;
import com.example.bookingapp.Models.Request.SendNotificationRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface NotificationUserService {
    Page<NotificationDTO> getAllByUser(String id_user, Integer pageNo);
    List<NotificationDTO> getAllByUser(String id_user);
    Object getById(String id_user, Long id_notify);
    Object deleteNotification(String id_user, DeleteRequest deleteRequest);
    Object updateStatusNotification(String userId, Long notify_id);
    Object sendNotificationToAll(SendNotificationRequest sendNotificationRequest);
    Object sendNotificationToUser(SendNotificationRequest sendNotificationRequest);
    void saveNotificationForUser(MessageNotifiDTO messageNotifiDTO, List<UserEntity> userEntities, StatusEntity statusEntity);
}
