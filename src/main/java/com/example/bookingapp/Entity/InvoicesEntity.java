package com.example.bookingapp.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "invoices")
public class InvoicesEntity {
    @Id
    @Column(name = "id_invoices")
    private String id_invoices;

    @Column(name = "total_amount")
    private Double total_amount;

    @Column(name = "total_discount")
    private Double total_discount;

    @Column(name = "paid_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate paidAt;

    @ManyToOne
    @JoinColumn(name = "method_id")
    private PaymentMethodEntity paymentMethodEntity;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private CustomerEntity customerEntity;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private StatusEntity statusEntity;

    @OneToOne
    @JoinColumn(name = "request_id")
    private RepairRequestEntity repairRequestEntity;

    @OneToMany(mappedBy = "invoicesEntity", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval = true)
    private List<DetailInvoicesEntity> detailInvoicesEntities = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "voucher_id")
    private CustomerVouchersEntity customerVouchersEntity;

    @Column(name = "created_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime updated_at;
}
