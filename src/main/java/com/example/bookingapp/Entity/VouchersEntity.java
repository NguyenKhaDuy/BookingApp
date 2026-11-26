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
@Table(name = "vouchers")
public class VouchersEntity {
    @Id
    @Column(name = "id_voucher")
    private String idVoucher;

    @Column(name = "code")
    private String code;

    @Column(name = "max_usage")
    private Long maxUsage;

    @Column(name = "used_count")
    private Long usedCount;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private VoucherStatus status;

    @Column(name = "created_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime updated_at;

    @OneToMany(mappedBy = "vouchersEntity", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval = true)
    private List<CustomerVouchersEntity> customerVouchersEntities = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "promotion_id")
    private PromotionsEntity promotionsEntity;
}
