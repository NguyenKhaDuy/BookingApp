package com.example.bookingapp.Models.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankLinkRequest {
    private String bankBin;
    private String bankCode;
    private String accountNumber;
}
