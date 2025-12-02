package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.OtpVerificationEntity;
import com.example.bookingapp.Entity.RoleEntity;
import com.example.bookingapp.Entity.UserEntity;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.MessageDTO;
import com.example.bookingapp.Models.DTO.OtpVerificationDTO;
import com.example.bookingapp.Models.DTO.UserDTO;
import com.example.bookingapp.Models.Request.DeleteRequest;
import com.example.bookingapp.Repository.OtpVerificationRepository;
import com.example.bookingapp.Services.OtpVerificationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class OtpVerificationServiceImpl implements OtpVerificationService {
    @Autowired
    OtpVerificationRepository otpVerificationRepository;
    @Autowired
    ModelMapper modelMapper;
    @Override
    public Page<OtpVerificationDTO> getAll(Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        Page<OtpVerificationEntity> otpVerificationEntities = otpVerificationRepository.findAll(pageable);
        List<OtpVerificationDTO> otpVerificationDTOS = new ArrayList<>();
        for (OtpVerificationEntity otpVerificationEntity : otpVerificationEntities){
            OtpVerificationDTO otpVerificationDTO = new OtpVerificationDTO();
            UserDTO userDTO = new UserDTO();
            modelMapper.map(otpVerificationEntity, otpVerificationDTO);
            otpVerificationDTO.setStatus(otpVerificationEntity.getStatusEntity().getNameStatus());

            //Lấy ra user đã dùng otp
            UserEntity userEntity = otpVerificationEntity.getUserEntity();
            modelMapper.map(userEntity, userDTO);
            for (RoleEntity roleEntity : userEntity.getRoleEntities()){
                userDTO.getRole_name().add(roleEntity.getRole_name());
            }
            otpVerificationDTO.setUserDTO(userDTO);

            //thêm vào trong danh sách otp
            otpVerificationDTOS.add(otpVerificationDTO);
        }
        return new PageImpl<>(otpVerificationDTOS, otpVerificationEntities.getPageable(), otpVerificationEntities.getTotalElements());
    }

    @Override
    public Object detailOtp(Long id_otp) {
        ErrorDTO errorDTO = new ErrorDTO();
        try {
            OtpVerificationEntity otpVerificationEntity = otpVerificationRepository.findById(id_otp).get();
            OtpVerificationDTO otpVerificationDTO = new OtpVerificationDTO();
            UserDTO userDTO = new UserDTO();
            modelMapper.map(otpVerificationEntity, otpVerificationDTO);
            otpVerificationDTO.setStatus(otpVerificationEntity.getStatusEntity().getNameStatus());

            //Lấy ra user đã dùng otp
            UserEntity userEntity = otpVerificationEntity.getUserEntity();
            modelMapper.map(userEntity, userDTO);
            for (RoleEntity roleEntity : userEntity.getRoleEntities()){
                userDTO.getRole_name().add(roleEntity.getRole_name());
            }
            otpVerificationDTO.setUserDTO(userDTO);
            return otpVerificationDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found otp");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object deleteOtp(DeleteRequest deleteRequest) {
        MessageDTO messageDTO = new MessageDTO();
        if (deleteRequest.getId() != null){
            otpVerificationRepository.deleteAllById(deleteRequest.getId());
            messageDTO.setMessage("Success");
            messageDTO.setHttpStatus(HttpStatus.OK);
        }
        messageDTO.setMessage("id otp can not null");
        messageDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
        return messageDTO;
    }
}
