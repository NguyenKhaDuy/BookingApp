package com.example.bookingapp.Models.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DepositResponse {
    private String transactionId;

    private Double amount;

    private String status;

    private String message;
}
