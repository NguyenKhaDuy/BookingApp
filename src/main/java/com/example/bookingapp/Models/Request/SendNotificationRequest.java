package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SendNotificationRequest {
    private String title;
    private String body;
    private LocalDateTime dateTime;
    private String type;
    private List<String> emailUser = new ArrayList<>();
}
