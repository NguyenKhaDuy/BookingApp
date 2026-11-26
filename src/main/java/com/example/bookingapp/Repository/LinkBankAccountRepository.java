package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LinkBankAccountRepository
        extends JpaRepository<LinkBankAccountEntity, String> {

    Optional<LinkBankAccountEntity>
    findByWalletEntity_IdWalletAndBankBinAndAccountNumber(
            String walletId,
            String bankBin,
            String accountNumber
    );

    Optional<LinkBankAccountEntity>
    findByIdAndWalletEntity_IdWalletAndStatus(
            String id,
            String walletId,
            BankLinkStatus status
    );

    List<LinkBankAccountEntity>
    findByWalletEntity_IdWalletAndStatus(
            String walletId,
            BankLinkStatus status
    );
}