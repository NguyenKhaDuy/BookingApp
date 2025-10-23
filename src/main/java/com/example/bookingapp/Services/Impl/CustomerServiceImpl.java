package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.CustomerEntity;
import com.example.bookingapp.Entity.RoleEntity;
import com.example.bookingapp.Models.DTO.CustomerDTO;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.MessageDTO;
import com.example.bookingapp.Models.DTO.RoleDTO;
import com.example.bookingapp.Models.Request.CustomerAvatarRequest;
import com.example.bookingapp.Models.Request.CustomerProfileRequest;
import com.example.bookingapp.Repository.CustomerRepository;
import com.example.bookingapp.Services.CustomerService;
import com.example.bookingapp.Utils.ConvertByteToBase64;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
public class CustomerServiceImpl implements CustomerService {
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    CustomerRepository customerRepository;
    @Override
    public Object getProfile(String id_customer) {
        ErrorDTO errorDTO = new ErrorDTO();
        CustomerDTO customerDTO = new CustomerDTO();
        try{
            CustomerEntity customerEntity = customerRepository.findById(id_customer).get();
            modelMapper.map(customerEntity, customerDTO);
            customerDTO.setAvatarBase64(ConvertByteToBase64.toBase64(customerEntity.getAvatar()));
            for (RoleEntity roleEntity : customerEntity.getRoleEntities()){
                RoleDTO roleDTO = new RoleDTO();
                roleDTO.setId_role(roleEntity.getId_role());
                roleDTO.setRole_name(roleEntity.getRole_name());
                customerDTO.getRoleDTOS().add(roleDTO);
            }
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found customer");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
        return customerDTO;
    }

    @Override
    public Object updateProfile(CustomerProfileRequest customerProfileRequest) {
        MessageDTO messageDTO = new MessageDTO();
        ErrorDTO errorDTO = new ErrorDTO();
        try{
            CustomerEntity customerEntity = customerRepository.findById(customerProfileRequest.getId_user()).get();
            try{
                modelMapper.map(customerProfileRequest, customerEntity);
                customerEntity.setUpdated_at(LocalDateTime.now());
                customerRepository.save(customerEntity);
                messageDTO.setMessage("Success");
                messageDTO.setHttpStatus(HttpStatus.OK);
                return messageDTO;
            }catch (RuntimeException ex){
                errorDTO.setMessage(ex.getMessage());
                errorDTO.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                return errorDTO;
            }
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found customer");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object updateAvatar(CustomerAvatarRequest customerAvatarRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageDTO messageDTO = new MessageDTO();
        try{
            CustomerEntity customerEntity = customerRepository.findById(customerAvatarRequest.getId_user()).get();
            try {
                customerEntity.setAvatar(customerAvatarRequest.getAvatar().getBytes());
                customerEntity.setUpdated_at(LocalDateTime.now());
            } catch (IOException e) {
                errorDTO.setMessage("Can not convert mutipartfile to byte");
                errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
                return errorDTO;
            }
            customerRepository.save(customerEntity);
            messageDTO.setMessage("Success");
            messageDTO.setHttpStatus(HttpStatus.OK);
            return messageDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found user");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }
}
