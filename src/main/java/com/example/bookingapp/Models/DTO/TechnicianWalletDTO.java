package com.example.bookingapp.Models.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TechnicianWalletDTO {
    private String idWallet;
    private float balance;
    private String code;
    private String technician_id;
    private String technician_name;
    List<LinkBankAccountDTO> linkBankAccountDTOS = new ArrayList<>();
}
