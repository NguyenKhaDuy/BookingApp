package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class OtpVerificationRequest {
    private Long id;
    private String otpCode;
    private String id_user;
    private String email;
    private String name_status;
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDateTime expires_at;
}
