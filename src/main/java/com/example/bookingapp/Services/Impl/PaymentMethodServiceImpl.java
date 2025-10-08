package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.PaymentMethodEntity;
import com.example.bookingapp.Models.DTO.PaymentMethodDTO;
import com.example.bookingapp.Repository.PaymentMethodRepository;
import com.example.bookingapp.Services.PaymentMethodService;
import com.example.bookingapp.Utils.ConvertByteToBase64;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentMethodServiceImpl implements PaymentMethodService {
    @Autowired
    PaymentMethodRepository paymentMethodRepository;
    @Autowired
    ModelMapper modelMapper;
    @Override
    public Page<PaymentMethodDTO> getAll(Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        Page<PaymentMethodEntity> paymentMethodEntities = paymentMethodRepository.findAll(pageable);
        List<PaymentMethodDTO> paymentMethodDTOS = new ArrayList<>();
        for (PaymentMethodEntity paymentMethodEntity : paymentMethodEntities){
            PaymentMethodDTO paymentMethodDTO = new PaymentMethodDTO();
            modelMapper.map(paymentMethodEntity, paymentMethodDTO);
            paymentMethodDTO.setIconBase64(ConvertByteToBase64.toBase64(paymentMethodEntity.getIcon()));
            paymentMethodDTOS.add(paymentMethodDTO);
        }
        return new PageImpl<>(paymentMethodDTOS, paymentMethodEntities.getPageable(), paymentMethodEntities.getTotalElements());
    }
}
