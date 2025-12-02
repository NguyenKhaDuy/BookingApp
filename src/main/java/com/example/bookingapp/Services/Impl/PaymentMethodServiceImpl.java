package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.PaymentMethodEntity;
import com.example.bookingapp.Entity.RoleEntity;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.MessageDTO;
import com.example.bookingapp.Models.DTO.PaymentMethodDTO;
import com.example.bookingapp.Models.DTO.RoleDTO;
import com.example.bookingapp.Models.Request.PaymentmethodRequest;
import com.example.bookingapp.Repository.PaymentMethodRepository;
import com.example.bookingapp.Services.PaymentMethodService;
import com.example.bookingapp.Utils.ConvertByteToBase64;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

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

    @Override
    public Object detailPaymentMethod(Long id_payment) {
        ErrorDTO errorDTO = new ErrorDTO();
        try{
            PaymentMethodEntity paymentMethodEntity = paymentMethodRepository.findById(id_payment).get();
            PaymentMethodDTO paymentMethodDTO = new PaymentMethodDTO();
            modelMapper.map(paymentMethodEntity, paymentMethodDTO);
            paymentMethodDTO.setIconBase64(ConvertByteToBase64.toBase64(paymentMethodEntity.getIcon()));
            return paymentMethodDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found payment");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object createPaymentMethod(PaymentmethodRequest paymentmethodRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageDTO messageDTO = new MessageDTO();
        try {
            PaymentMethodEntity paymentMethodEntity = new PaymentMethodEntity();
            modelMapper.map(paymentmethodRequest, paymentMethodEntity);
            if (paymentmethodRequest.getIconBase64() != null){
                paymentMethodEntity.setIcon(paymentmethodRequest.getIconBase64().getBytes());
            }
            paymentMethodEntity.setCreated_at(LocalDateTime.now());
            paymentMethodEntity.setUpdated_at(LocalDateTime.now());
            paymentMethodRepository.save(paymentMethodEntity);
            messageDTO.setMessage("Success");
            messageDTO.setHttpStatus(HttpStatus.OK);
            return messageDTO;
        }catch (Exception ex){
            errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
            errorDTO.setMessage("Fail");
            return errorDTO;
        }
    }

    @Override
    public Object updatePaymentMethod(PaymentmethodRequest paymentmethodRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageDTO messageDTO = new MessageDTO();
        try {
            PaymentMethodEntity paymentMethodEntity = paymentMethodRepository.findById(paymentmethodRequest.getId_method()).get();
            modelMapper.map(paymentmethodRequest, paymentMethodEntity);
            if (paymentmethodRequest.getIconBase64() != null){
                paymentMethodEntity.setIcon(paymentmethodRequest.getIconBase64().getBytes());
            }
            paymentMethodEntity.setUpdated_at(LocalDateTime.now());
            paymentMethodRepository.save(paymentMethodEntity);
            messageDTO.setMessage("Success");
            messageDTO.setHttpStatus(HttpStatus.OK);
            return messageDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found payment");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object deletePaymentMethod(Long id_payment) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageDTO messageDTO = new MessageDTO();
        try {
            PaymentMethodEntity paymentMethodEntity = paymentMethodRepository.findById(id_payment).get();
            paymentMethodRepository.delete(paymentMethodEntity);
            messageDTO.setMessage("Success");
            messageDTO.setHttpStatus(HttpStatus.OK);
            return messageDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found payment");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }
}
