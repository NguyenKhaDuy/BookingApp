package com.example.bookingapp.Services;

import org.springframework.stereotype.Service;

@Service
public interface WebSocketService {
    public void sendAllUser(String title, String body);
    public void sendPrivateUser( String useName ,String title, String body);
}
