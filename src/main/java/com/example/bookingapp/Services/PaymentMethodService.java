package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.PaymentMethodDTO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface PaymentMethodService {
    Page<PaymentMethodDTO> getAll(Integer pageNo);
}
