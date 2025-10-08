package com.example.bookingapp.API;

import com.example.bookingapp.Models.DTO.DataDTO;
import com.example.bookingapp.Models.DTO.PaymentMethodDTO;
import com.example.bookingapp.Services.PaymentMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentMethodApi {
    @Autowired
    PaymentMethodService paymentMethodService;
    @GetMapping("/api/paymentmethod/")
    public ResponseEntity<DataDTO> getAll(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo){
        DataDTO dataDTO = new DataDTO();
        Page<PaymentMethodDTO> paymentMethodDTOS = paymentMethodService.getAll(pageNo);
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setTotal_page(paymentMethodDTOS.getTotalPages());
        dataDTO.setData(paymentMethodDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }
}
