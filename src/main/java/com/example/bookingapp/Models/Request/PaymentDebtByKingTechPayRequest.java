package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentDebtByKingTechPayRequest {
    private String technicianId;
    private Double amount;
}
