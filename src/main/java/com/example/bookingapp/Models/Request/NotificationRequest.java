package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationRequest {
    private Long id_notify;
    private String title;
    private String message;
}
