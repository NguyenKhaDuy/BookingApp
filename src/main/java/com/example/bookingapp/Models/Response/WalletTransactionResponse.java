package com.example.bookingapp.Models.Response;

import com.example.bookingapp.Entity.WalletTransactionType;
import com.example.bookingapp.Entity.WalletTransactionStatus;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WalletTransactionResponse {

    private String id;

    private WalletTransactionType type;

    private WalletTransactionStatus status;

    private Double amount;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime createdAt;
}