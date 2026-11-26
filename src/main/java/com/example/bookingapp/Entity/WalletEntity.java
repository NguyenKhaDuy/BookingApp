package com.example.bookingapp.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "wallet")
public class WalletEntity {

    @Id
    @Column(name = "id")
    private String idWallet;

    @Column(nullable = false)
    private Double balance = (double)0;

    // Mã PIN 6 số được băm bằng BCrypt
    @Column(name = "code", length = 255)
    private String code;

    @Version
    private Long version;

    // Một Technician chỉ có một ví KingTech Pay
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private UserEntity userEntity;


    // Một ví liên kết nhiều ngân hàng
    @OneToMany(
            mappedBy = "walletEntity",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    private List<LinkBankAccountEntity> linkBankAccountEntities =
            new ArrayList<>();

    // Một ví có nhiều giao dịch
    @OneToMany(
            mappedBy = "wallet",
            fetch = FetchType.LAZY
    )
    private List<WalletTransactionEntity> walletTransactionEntities =
            new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (idWallet == null) {
            idWallet = UUID.randomUUID().toString();
        }
    }
}