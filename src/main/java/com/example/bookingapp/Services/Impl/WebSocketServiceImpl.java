package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Models.DTO.MessageNotifiDTO;
import com.example.bookingapp.Services.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WebSocketServiceImpl implements WebSocketService {
    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void sendAllUser(MessageNotifiDTO messageNotifiDTO) {
        simpMessagingTemplate.convertAndSend("/topic/notify", messageNotifiDTO);
    }

    @Override
    public void sendPrivateUser(String email, MessageNotifiDTO messageNotifiDTO) {
        // gửi tới user
        simpMessagingTemplate.convertAndSendToUser(
                email
                ,
                "/queue/notify",
                messageNotifiDTO
        );
    }
}
