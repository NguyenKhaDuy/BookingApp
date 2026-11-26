
package com.example.bookingapp.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "link_bank_account")
public class LinkBankAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "wallet_id",
            referencedColumnName = "id",
            nullable = false
    )
    private WalletEntity walletEntity;

    @Column(name = "bank_bin")
    private String bankBin;

    @OneToMany(mappedBy = "bankAccount", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval = true)
    private List<WalletTransactionEntity> walletTransactionEntities = new ArrayList<>();

    @Column(name = "bank_code")
    private String bankCode;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "lookup_verified")
    private boolean lookupVerified = false;

    @Column(name = "ownership_verified")
    private boolean ownershipVerified = false;

    @Enumerated(EnumType.STRING)
    private BankLinkStatus status;

    @Column(name = "linked_at")
    private LocalDateTime linkedAt;
}
