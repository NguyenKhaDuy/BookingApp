package com.example.bookingapp.Models.DTO;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class MessageDTO {
    private String message;
    private HttpStatus httpStatus;
}
