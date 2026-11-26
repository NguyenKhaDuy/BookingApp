package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WithdrawRequest {
    private Double amount;

    private String idempotencyKey;

    private String bankAccountId;

    private String pin;
}
