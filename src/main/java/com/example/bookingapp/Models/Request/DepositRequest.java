package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DepositRequest {
    private Double amount;
    //mã giáo dịch
    private String idempotencyKey;
    private String bankAccountId;
}
