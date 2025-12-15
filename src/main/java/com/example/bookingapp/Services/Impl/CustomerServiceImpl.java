package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.CustomerEntity;
import com.example.bookingapp.Entity.RoleEntity;
import com.example.bookingapp.Models.DTO.CustomerDTO;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.Request.UpdateEmailRequest;
import com.example.bookingapp.Models.Response.MessageResponse;
import com.example.bookingapp.Models.DTO.RoleDTO;
import com.example.bookingapp.Models.Request.AvatarRequest;
import com.example.bookingapp.Models.Request.ProfileRequest;
import com.example.bookingapp.Repository.CustomerRepository;
import com.example.bookingapp.Services.CustomerService;
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
                roleDTO.setRole_name(roleEntity.getRoleName());
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
    public Object updateProfile(ProfileRequest profileRequest) {
        MessageResponse messageResponse = new MessageResponse();
        ErrorDTO errorDTO = new ErrorDTO();
        try{
            CustomerEntity customerEntity = customerRepository.findById(profileRequest.getId_user()).get();
            try{
                modelMapper.map(profileRequest, customerEntity);
                customerEntity.setUpdated_at(LocalDateTime.now());
                customerRepository.save(customerEntity);
                messageResponse.setMessage("Success");
                messageResponse.setHttpStatus(HttpStatus.OK);
                return messageResponse;
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
    public Object updateAvatar(AvatarRequest avatarRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageResponse messageResponse = new MessageResponse();
        try{
            CustomerEntity customerEntity = customerRepository.findById(avatarRequest.getId_user()).get();
            try {
                customerEntity.setAvatar(avatarRequest.getAvatar().getBytes());
                customerEntity.setUpdated_at(LocalDateTime.now());
            } catch (IOException e) {
                errorDTO.setMessage("Can not convert mutipartfile to byte");
                errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
                return errorDTO;
            }
            customerRepository.save(customerEntity);
            messageResponse.setMessage("Success");
            messageResponse.setHttpStatus(HttpStatus.OK);
            return messageResponse;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found user");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Page<CustomerDTO> getAllCustomer(Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        Page<CustomerEntity> customerEntities = customerRepository.findAll(pageable);
        List<CustomerDTO> customerDTOS = new ArrayList<>();
        for (CustomerEntity customerEntity : customerEntities){
            CustomerDTO customerDTO = new CustomerDTO();
            modelMapper.map(customerEntity, customerDTO);
            customerDTO.setAvatarBase64(ConvertByteToBase64.toBase64(customerEntity.getAvatar()));
            for (RoleEntity roleEntity : customerEntity.getRoleEntities()){
                RoleDTO roleDTO = new RoleDTO();
                roleDTO.setId_role(roleEntity.getId_role());
                roleDTO.setRole_name(roleEntity.getRoleName());
                customerDTO.getRoleDTOS().add(roleDTO);
            }
            customerDTOS.add(customerDTO);
        }
        return new PageImpl<>(customerDTOS, customerEntities.getPageable(), customerEntities.getTotalElements());
    }

    @Override
    public Object updateEmail(UpdateEmailRequest updateEmailRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageResponse messageResponse = new MessageResponse();
        try{
            CustomerEntity customerEntity = customerRepository.findByEmail(updateEmailRequest.getOld_email());
            customerEntity.setEmail(updateEmailRequest.getNew_email());
            customerRepository.save(customerEntity);
            messageResponse.setMessage("Success");
            messageResponse.setHttpStatus(HttpStatus.OK);
            return messageResponse;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found email");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }
}
