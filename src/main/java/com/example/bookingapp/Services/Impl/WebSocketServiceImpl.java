package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Models.DTO.MessageNotifiDTO;
import com.example.bookingapp.Services.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WebSocketServiceImpl implements WebSocketService {
    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void sendAllUser(String title, String body) {
//        MessageNotifiDTO msg = new MessageNotifiDTO();
//        msg.setTitle(title);
//        msg.setBody(body);
//        msg.setDateTime(LocalDateTime.now());
//        msg.setUserName("SYSTEM");
//
//        simpMessagingTemplate.convertAndSend("/all/messages", msg);
    }

    @Override
    public void sendPrivateUser(String email, String title, String body) {
        MessageNotifiDTO msg = new MessageNotifiDTO();
        msg.setTitle(title);
        msg.setBody(body);
        msg.setEmail(email);
        msg.setDateTime(LocalDateTime.now());

        // gửi tới user
        simpMessagingTemplate.convertAndSendToUser(
                email,
                "/queue/specific",
                msg
        );

        System.out.println("Sent WS notification to: " + email);
    }
}
