package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationRequest {
    private String id_notify;
    private String title;
    private String id_type;
    private String message;
}
