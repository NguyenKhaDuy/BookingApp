package com.example.bookingapp.Models.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinkBankAccountDTO {
    private Long id;
    private String bank;
    private String bankNumber;
    private String cardNumber;
}
