package com.example.bookingapp.API;

import com.example.bookingapp.Models.DTO.DataDTO;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.PromotionDTO;
import com.example.bookingapp.Models.Request.PromotionRequest;
import com.example.bookingapp.Services.PromotionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PromotionApi {
    @Autowired
    PromotionsService promotionsService;

    @GetMapping(value = "/api/admin/promotions")
    public ResponseEntity<Object> getAllPromotions(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo) {
        DataDTO dataDTO = new DataDTO();
        Page<PromotionDTO> promotionDTOS = promotionsService.getPromotions(pageNo);
        dataDTO.setData(promotionDTOS.getContent());
        dataDTO.setMessage("Successfully retrieved all the promotions");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setTotal_page(promotionDTOS.getTotalPages());
        dataDTO.setCurrent_page(pageNo);
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/api/admin/promotion/id={id}")
    public ResponseEntity<Object> getPromotionById(@PathVariable("id") String id) {
        Object result = promotionsService.getPromotion(id);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping(value = "/api/admin/promotion/id={id}")
    public ResponseEntity<Object> deletePromotion(@PathVariable("id") String id) {
        Object result = promotionsService.deletePromotion(id);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value = "/api/admin/promotion")
    public ResponseEntity<Object> createPromotion(@RequestBody PromotionRequest promotionRequest) {
        Object result = promotionsService.addPromotion(promotionRequest);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping(value = "/api/admin/promotion")
    public ResponseEntity<Object> updatePromotion(@RequestBody PromotionRequest promotionRequest) {
        Object result = promotionsService.updatePromotion(promotionRequest);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
