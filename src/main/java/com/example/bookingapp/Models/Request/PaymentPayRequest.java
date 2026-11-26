package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentPayRequest {
    private String idInvoice;
    private String idVoucher;
}
