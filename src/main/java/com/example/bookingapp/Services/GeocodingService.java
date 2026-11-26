package com.example.bookingapp.Services;

import com.example.bookingapp.Models.Response.GeocodingResponse;
import org.springframework.stereotype.Service;

@Service
public interface GeocodingService {
    GeocodingResponse getCoordinates(String address);
}
