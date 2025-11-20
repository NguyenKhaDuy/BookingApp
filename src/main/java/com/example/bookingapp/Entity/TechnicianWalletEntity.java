package com.example.bookingapp.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "technician_wallet")
public class TechnicianWalletEntity {
    @Id
    @Column(name = "id")
    private String idWallet;

    @Column(name = "balance")
    private float balance;

    //Code này sẽ là 6 số dùng để nạp tiền hoặc rút tiền sẽ được mã hóa
    @Column(name = "code")
    private String code;

    @OneToMany(mappedBy = "technicianWalletEntity", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval = true)
    private List<LinkBankAccountEntity> linkBankAccountEntities = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "technician_id")
    private TechnicianEntity technicianEntity;
}
