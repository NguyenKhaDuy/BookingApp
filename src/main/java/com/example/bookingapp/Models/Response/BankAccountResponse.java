package com.example.bookingapp.Models.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BankAccountResponse {
    private String id;
    private String bankBin;
    private String bankCode;
    private String accountNumber;
    private String accountName;
    private boolean lookupVerified;
    private boolean ownershipVerified;
}
