package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.PromotionDTO;
import com.example.bookingapp.Models.Request.PromotionRequest;
import com.example.bookingapp.Models.Response.MessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface PromotionsService {
    Page<PromotionDTO> getPromotions(Integer pageNo);
    Object getPromotion(String promotionId);
    Object deletePromotion(String promotionId);
    Object addPromotion(PromotionRequest promotionRequest);
    Object updatePromotion(PromotionRequest promotionRequest);
}
