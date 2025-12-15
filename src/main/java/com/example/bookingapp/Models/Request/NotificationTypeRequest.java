package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationTypeRequest {
    private Long id;
    private String type;
    private String description;
}
