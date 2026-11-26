package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeWalletPinRequest {
    private String oldPin;
    private String newPin;
}
