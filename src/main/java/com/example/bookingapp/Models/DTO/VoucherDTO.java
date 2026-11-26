package com.example.bookingapp.Models.DTO;

import com.example.bookingapp.Entity.PromotionStatus;
import com.example.bookingapp.Entity.VoucherStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class VoucherDTO {
    private String idVoucher;
    private String code;
    private Long maxUsage;
    private Long usedCount;
    private VoucherStatus status;
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime created_at;
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime updated_at;
    private String idPromotion;
    private String name;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate startDate;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate endDate;
    private PromotionStatus promotionStatus;
    private Long discountValue;
    private Long maxDiscount;
    private Long minOrderValue;
    private List<ServiceVoucher> ServiceVoucher;
}
