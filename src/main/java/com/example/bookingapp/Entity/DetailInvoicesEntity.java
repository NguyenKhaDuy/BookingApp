package com.example.bookingapp.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "detail_invoice")
public class DetailInvoicesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_detail_invoice;

    @Column(name = "name")
    private String name;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price")
    private Float price;

    @Column(name = "total_price")
    private Float total_price;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private InvoicesEntity invoicesEntity;
}
