package com.example.bookingapp.Models.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawResponse {
    private String transactionId;
    private Double amount;
    private String status;
    private String message;
    private LocalDateTime createdAt;
}
