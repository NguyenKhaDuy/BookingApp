package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.PaymentMethodDTO;
import com.example.bookingapp.Models.Request.PaymentmethodRequest;
import com.example.bookingapp.Models.Request.RoleRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface PaymentMethodService {
    Page<PaymentMethodDTO> getAll(Integer pageNo);
    Object detailPaymentMethod(Long id_payment);
    Object createPaymentMethod(PaymentmethodRequest paymentmethodRequest);
    Object updatePaymentMethod(PaymentmethodRequest paymentmethodRequest);
    Object deletePaymentMethod(Long id_payment);
}
