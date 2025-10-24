package com.example.bookingapp.Services;

import com.example.bookingapp.Models.Request.AvatarRequest;
import com.example.bookingapp.Models.Request.ProfileRequest;
import org.springframework.stereotype.Service;

@Service
public interface CustomerService {
    Object getProfile(String id_customer);
    Object updateProfile(ProfileRequest profileRequest);
    Object updateAvatar(AvatarRequest avatarRequest);
}
