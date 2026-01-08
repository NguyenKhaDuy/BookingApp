package com.example.bookingapp.Models.DTO;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class LoginDTO {
    private String message;
    private String token;
    private String id_user;
    private String full_name;
    private String avatarBase64;
    private List<String> roles = new ArrayList<>();
    private HttpStatus httpStatus;

}
