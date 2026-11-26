package com.example.bookingapp.Models.DTO;

import com.example.bookingapp.Entity.PromotionStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PromotionDTO {
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
    private List<PromotionServiceDTO> promotionServiceDTOS;
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime created_at;
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime updated_at;
}
