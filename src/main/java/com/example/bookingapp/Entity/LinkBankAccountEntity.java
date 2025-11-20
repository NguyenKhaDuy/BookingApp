package com.example.bookingapp.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "link_bank_account")
public class LinkBankAccountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bank")
    private String bank;

    @Column(name = "bank_number")
    private String bankNumber;

    @Column(name = "card_number")
    private String cardNumber;

    @ManyToOne
    @JoinColumn(name = "wallet_id")
    private TechnicianWalletEntity technicianWalletEntity;
}
