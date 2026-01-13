package com.example.bookingapp.API;

import com.example.bookingapp.Models.DTO.DataDTO;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.PaymentMethodDTO;
import com.example.bookingapp.Models.Request.PaymentmethodRequest;
import com.example.bookingapp.Models.Request.RoleRequest;
import com.example.bookingapp.Services.PaymentMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PaymentMethodApi {
    @Autowired
    PaymentMethodService paymentMethodService;
    @GetMapping("/api/admin/paymentmethod/")
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

    @GetMapping("/api/paymentmethod/")
    public ResponseEntity<DataDTO> getAll(){
        DataDTO dataDTO = new DataDTO();
        List<PaymentMethodDTO> paymentMethodDTOS = paymentMethodService.getAll();
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setData(paymentMethodDTOS);
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/api/admin/payment/id-payment={id_payment}")
    public ResponseEntity<Object> detailRole(@PathVariable Long id_payment){
        DataDTO dataDTO = new DataDTO();
        Object result = paymentMethodService.detailPaymentMethod(id_payment);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setData(result);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value = "/api/admin/payment/")
    public ResponseEntity<Object> createPayment(@ModelAttribute PaymentmethodRequest paymentmethodRequest){
        Object result = paymentMethodService.createPaymentMethod(paymentmethodRequest);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PutMapping(value = "/api/admin/payment/")
    public ResponseEntity<Object> updatePayment(@ModelAttribute PaymentmethodRequest paymentmethodRequest){
        Object result = paymentMethodService.updatePaymentMethod(paymentmethodRequest);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping(value = "/api/admin/payment/id-payment={id_payment}")
    public ResponseEntity<Object> deletePayment(@PathVariable Long id_payment){
        Object result = paymentMethodService.deletePaymentMethod(id_payment);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
