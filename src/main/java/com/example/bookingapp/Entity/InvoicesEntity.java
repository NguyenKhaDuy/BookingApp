package com.example.bookingapp.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "invoices")
public class InvoicesEntity {
    @Id
    @Column(name = "id_invoices")
    private String id_invoices;

    @Column(name = "total_amount")
    private Float total_amount;

    @Column(name = "paid_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate paid_at;

    @ManyToOne
    @JoinColumn(name = "method_id")
    private PaymentMethodEntity paymentMethodEntity;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private CustomerEntity customerEntity;

    @OneToOne
    @JoinColumn(name = "request_id")
    private RepairRequestEntity repairRequestEntity;

    @Column(name = "created_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime updated_at;
}
