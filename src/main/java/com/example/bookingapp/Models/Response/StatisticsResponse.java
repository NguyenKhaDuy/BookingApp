package com.example.bookingapp.Models.Response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class StatisticsResponse {
    private float data;
    private String message;
    private HttpStatus httpStatus;
}
