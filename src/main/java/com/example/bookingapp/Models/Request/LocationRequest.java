package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationRequest {
    private Long id_location;
    private String district;
    private String ward;
    private String conscious;
}

