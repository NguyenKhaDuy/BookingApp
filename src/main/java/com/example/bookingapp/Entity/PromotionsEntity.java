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
@Table(name = "promotions")
public class PromotionsEntity {
    @Id
    @Column(name = "id_promotion")
    private String idPromotion;

    @Column(name = "name")
    private String name;

    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    @Column(name = "discount_value")
    private Long discountValue;

    @Column(name = "max_discount")
    private Long maxDiscount;

    @Column(name = "min_order_value")
    private Long minOrderValue;

    @Column(name = "total_quantity")
    private Long totalQuantity;

    @Column(name = "start_date")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate startDate;

    @Column(name = "end_date")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate endDate;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private PromotionStatus promotionStatus;

    @Column(name = "created_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime updated_at;

    @OneToMany(mappedBy = "promotionsEntity", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval = true)
    private List<VouchersEntity> vouchersEntities = new ArrayList<>();

    @OneToMany(mappedBy = "promotionsEntity", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval = true)
    private List<PromotionServiceEntity> promotionServiceEntities = new ArrayList<>();
}
