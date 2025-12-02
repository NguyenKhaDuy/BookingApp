package com.example.bookingapp.API;

import com.example.bookingapp.Models.DTO.DataDTO;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.MessageDTO;
import com.example.bookingapp.Models.DTO.OtpVerificationDTO;
import com.example.bookingapp.Models.Request.DeleteRequest;
import com.example.bookingapp.Services.OtpVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class OtpVerificationApi {
    @Autowired
    OtpVerificationService otpVerificationService;
    @GetMapping(value = "/api/admin/otp-verification/")
    public ResponseEntity<Object> getAll(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo){
        DataDTO dataDTO = new DataDTO();
        Page<OtpVerificationDTO> otpVerificationDTOS = otpVerificationService.getAll(pageNo);
        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setTotal_page(otpVerificationDTOS.getTotalPages());
        dataDTO.setData(otpVerificationDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/api/admin/otp-verification/id-otp={id_otp}")
    public ResponseEntity<Object> detailOtp(@PathVariable Long id_otp){
        DataDTO dataDTO = new DataDTO();
        Object result = otpVerificationService.detailOtp(id_otp);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setData(result);
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @DeleteMapping(value = "/api/admin/otp-verification/")
    public ResponseEntity<Object> deleteOtp(@RequestBody DeleteRequest deleteRequest){
        Object result = otpVerificationService.deleteOtp(deleteRequest);
        return new ResponseEntity<>(result, ((MessageDTO) result).getHttpStatus());
    }

}
