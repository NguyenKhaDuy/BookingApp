package com.example.bookingapp.Services;

import com.example.bookingapp.Models.Request.LoginRequest;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    String login(LoginRequest loginRequest);
}
