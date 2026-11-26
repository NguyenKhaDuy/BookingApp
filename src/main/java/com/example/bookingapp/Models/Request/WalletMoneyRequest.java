package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class WalletMoneyRequest {
    private String linkedBankId;
    private BigDecimal amount;
    private String idempotencyKey;
}
