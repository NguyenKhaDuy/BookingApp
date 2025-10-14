package com.example.bookingapp.Models.DTO;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@Setter
public class ErrorDTO{
    private String message;
    private HttpStatus httpStatus;
}
