package com.example.bookingapp.Models.Request;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceRequest {
    private Long id_service;
    private String name_service;
}
