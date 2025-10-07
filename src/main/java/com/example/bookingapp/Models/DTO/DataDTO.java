package com.example.bookingapp.Models.DTO;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class DataDTO<T> {
    private String message;
    private HttpStatus httpStatus;
    private Integer current_page;
    private Integer total_page;
    private T data;
}
