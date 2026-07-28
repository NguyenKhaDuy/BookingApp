package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentDebtRequest {
    private String id_technician;
    private Long amount;
    private String bank;
}
