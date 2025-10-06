package com.example.bookingapp.Api;

import com.example.bookingapp.Entity.UserEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserApi {
    @GetMapping(value = "/api/user")
    public UserEntity user() {
        return new UserEntity();
    }
}
