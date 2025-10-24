package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcceptRequest {
    private String id_technician;
    private Long id_request;
}
