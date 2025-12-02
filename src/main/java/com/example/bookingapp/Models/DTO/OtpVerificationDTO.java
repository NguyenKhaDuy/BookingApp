package com.example.bookingapp.Models.DTO;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class OtpDTO {
    private Long id_otp;
    private String otp_code;
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime expires_at;
    private UserDTO userDTO;
    private String status;
}
