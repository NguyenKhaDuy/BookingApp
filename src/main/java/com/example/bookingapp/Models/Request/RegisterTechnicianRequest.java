package com.example.bookingapp.Models.Request;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterTechnicianRequest extends RegisterCustomerRequest{
    private String working_area;
    private Integer experience_year;
    //mặc định công nợ của thợ là 0
    //mặc định level của thợ là level thấp
}
