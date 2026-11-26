package com.example.bookingapp.Models.Request;

import com.example.bookingapp.Entity.PromotionStatus;
import com.example.bookingapp.Models.DTO.PromotionServiceDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PromotionRequest {
    private String idPromotion;
    private String name;
    private String description;
    private Long discountValue;
    private Long maxDiscount;
    private Long minOrderValue;
    private Long totalQuantity;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate startDate;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate endDate;
    private PromotionStatus promotionStatus;
    //Dùng cho trường hợp thêm mới
    private List<String> idServices;
}
