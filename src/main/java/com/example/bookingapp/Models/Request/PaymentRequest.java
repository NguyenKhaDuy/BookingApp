package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {
    private String bank;
    private Float amount;
    private String id_request;
    private String requestType;
}
