package com.example.bookingapp.Models.DTO;

import com.example.bookingapp.Entity.VoucherStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class CustomerVouchersDTO {
    private String idCustomerVoucher;
    private Long usageLimit; //giới hạn voucher của phía người dùng
    private Long usageCount; //số lượt dùng của phía người dùng
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime receivedAt;
    private VoucherStatus status;
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime expiresAt;
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime usedAt;
    private String idVoucher;
    private String code;
    private Long maxUsage; //số lượt dùng tối đa của voucher
    private Long usedCount; // số lượt đã sử dụng của voucher
    private Long discountValue;
    private Long maxDiscount;
    private Long minOrderValue;
    private String idPromotion;
    private String name;
    private List<ServiceVoucher> serviceVouchers;
}
