package com.example.bookingapp.Models.Request;

import com.example.bookingapp.Entity.VoucherStatus;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoucherRequest {
    private String idVoucher;
    private String code;
    private Long maxUsage;
    private Long usedCount;
    private String promotionId;
    private VoucherStatus status;
}
