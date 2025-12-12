package com.example.bookingapp.Models.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MessageNotifiDTO {
    private String title;
    private String body;
    private LocalDateTime dateTime;
    private String email;
}
