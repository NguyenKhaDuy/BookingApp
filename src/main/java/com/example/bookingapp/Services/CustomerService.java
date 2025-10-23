package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.CustomerDTO;
import com.example.bookingapp.Models.Request.CustomerAvatarRequest;
import com.example.bookingapp.Models.Request.CustomerProfileRequest;
import org.springframework.stereotype.Service;

@Service
public interface CustomerService {
    Object getProfile(String id_customer);
    Object updateProfile(CustomerProfileRequest customerProfileRequest);
    Object updateAvatar(CustomerAvatarRequest customerAvatarRequest);
}
