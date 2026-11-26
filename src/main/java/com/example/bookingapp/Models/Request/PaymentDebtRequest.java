package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentDebtRequest {
    private Long amount;
    private String bank;
    private String id_technician;
    private String userAgent;
}
