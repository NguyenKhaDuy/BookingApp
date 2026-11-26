package com.example.bookingapp.Services;

import org.springframework.stereotype.Service;

@Service
public interface BankAccountLookupService {
    String lookupAccount(
            String bankCode,
            String accountNumber
    );
}
