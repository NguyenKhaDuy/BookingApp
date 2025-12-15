package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.LevelDTO;
import com.example.bookingapp.Models.DTO.NotificationTypeDTO;
import com.example.bookingapp.Models.Request.LevelRequest;
import com.example.bookingapp.Models.Request.NotificationTypeRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface NotificationTypeService {
    Page<NotificationTypeDTO> getAll(Integer pageNo);
    Object detailNotifyType(Long id_type);
    Object createNotifyType(NotificationTypeRequest notificationTypeRequest);
    Object updateNotifyType(NotificationTypeRequest notificationTypeRequest);
    Object deleteNotifyType(Long id_type);
}
