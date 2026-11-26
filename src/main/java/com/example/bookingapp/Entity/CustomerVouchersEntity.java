package com.example.bookingapp.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "customer_voucher")
public class CustomerVouchersEntity {
    @Id
    @Column(name = "id_customer_voucher")
    private String idCustomerVoucher;

    @Column(name = "usage_limit")
    private Long usageLimit;

    @Column(name = "usage_count")
    private Long usageCount;

    @Column(name = "received_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime receivedAt;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private VoucherStatus status;

    @Column(name = "expires_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime usedAt;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private CustomerEntity customerEntity;

    @OneToMany(mappedBy = "customerVouchersEntity", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval = true)
    private List<InvoicesEntity> invoicesEntities = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "voucher_id")
    private VouchersEntity vouchersEntity;
}
