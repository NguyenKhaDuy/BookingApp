package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.MessageNotifiDTO;
import org.springframework.stereotype.Service;

@Service
public interface WebSocketService {
    public void sendAllUser(MessageNotifiDTO messageNotifiDTO);
    public void sendPrivateUser(String email, MessageNotifiDTO messageNotifiDTO);
}
