package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SendNotificationRequest {
    private String id_notification;
    private LocalDateTime dateTime;
    private List<String> emailUser = new ArrayList<>();
}
