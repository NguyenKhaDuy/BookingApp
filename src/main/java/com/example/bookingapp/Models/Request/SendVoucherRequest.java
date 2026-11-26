package com.example.bookingapp.Models.Request;

import com.example.bookingapp.Entity.TypeSendVoucher;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SendVoucherRequest {
    private String voucherId;
    private TypeSendVoucher sendVoucherType;
    private String serviceId;
    private Long minValueInvoice;
    private Long usageLimit;
    private LocalDateTime expiresAt;
    private Integer month;
}
