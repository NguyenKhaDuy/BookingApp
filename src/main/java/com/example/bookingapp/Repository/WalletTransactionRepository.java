package com.example.bookingapp.Repository;

import com.example.bookingapp.Entity.WalletTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WalletTransactionRepository
        extends JpaRepository<WalletTransactionEntity, String> {


    Optional<WalletTransactionEntity> findByIdempotencyKey(
            String idempotencyKey
    );

    List<WalletTransactionEntity>
    findByWallet_IdWalletOrderByCreatedAtDesc(
            String walletId
    );
    boolean existsByIdempotencyKey(String idempotencyKey);
}