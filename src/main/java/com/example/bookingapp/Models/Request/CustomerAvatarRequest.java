package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class CustomerAvatarRequest {
    private String id_user;
    private MultipartFile avatar;
}
