package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.OtpVerificationDTO;
import com.example.bookingapp.Models.Request.DeleteRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface OtpVerificationService {
    Page<OtpVerificationDTO> getAll(Integer pageNo);
    Object detailOtp(Long id_otp);
    Object deleteOtp(DeleteRequest deleteRequest);
}
