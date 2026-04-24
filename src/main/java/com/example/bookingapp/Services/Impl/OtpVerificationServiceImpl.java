package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.OtpVerificationEntity;
import com.example.bookingapp.Entity.RoleEntity;
import com.example.bookingapp.Entity.StatusEntity;
import com.example.bookingapp.Entity.UserEntity;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.Request.OtpVerificationRequest;
import com.example.bookingapp.Models.Response.MessageResponse;
import com.example.bookingapp.Models.DTO.OtpVerificationDTO;
import com.example.bookingapp.Models.DTO.UserDTO;
import com.example.bookingapp.Models.Request.DeleteRequest;
import com.example.bookingapp.Repository.OtpVerificationRepository;
import com.example.bookingapp.Repository.StatusRepository;
import com.example.bookingapp.Repository.UserRepository;
import com.example.bookingapp.Services.OtpVerificationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class OtpVerificationServiceImpl implements OtpVerificationService {
    @Autowired
    OtpVerificationRepository otpVerificationRepository;
    @Autowired
    StatusRepository statusRepository;
    @Autowired
    UserRepository userRepository;
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
                userDTO.getRole_name().add(roleEntity.getRoleName());
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
                userDTO.getRole_name().add(roleEntity.getRoleName());
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
        MessageResponse messageResponse = new MessageResponse();
        if (deleteRequest.getId() != null){
            otpVerificationRepository.deleteAllById(deleteRequest.getId());
            messageResponse.setMessage("Success");
            messageResponse.setHttpStatus(HttpStatus.OK);
        }else{
            messageResponse.setMessage("id otp can not null");
            messageResponse.setHttpStatus(HttpStatus.BAD_REQUEST);
        }
        return messageResponse;
    }

    @Override
    public Object saveOtp(OtpVerificationRequest otpVerificationRequest) {
        OtpVerificationEntity otpVerification = new OtpVerificationEntity();
        try {
            StatusEntity statusEntity = statusRepository.findByNameStatus(otpVerificationRequest.getName_status());

            otpVerification.setOtp_code(otpVerificationRequest.getOtpCode());
            otpVerification.setStatusEntity(statusEntity);
            if(otpVerificationRequest.getId_user() != null){
                UserEntity userEntity = userRepository.findById(otpVerificationRequest.getId_user()).get();
                otpVerification.setUserEntity(userEntity);
            }
            otpVerification.setEmail(otpVerificationRequest.getEmail());
            otpVerification.setCreated_at(LocalDateTime.now());
            otpVerification.setExpires_at(otpVerificationRequest.getExpires_at());
            otpVerification.setUpdated_at(LocalDateTime.now());
            otpVerificationRepository.save(otpVerification);
            MessageResponse messageResponse = new MessageResponse();
            messageResponse.setMessage("Success");
            messageResponse.setHttpStatus(HttpStatus.OK);
            return messageResponse;
        }catch (NoSuchElementException ex)
        {
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setMessage("Can not found status");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public List<OtpVerificationDTO> getByEmail(String email) {
        List<OtpVerificationEntity> otpVerificationEntities = otpVerificationRepository.findByEmailOrderByIdDesc(email);
        List<OtpVerificationDTO> otpVerificationDTOS = new ArrayList<>();
        for (OtpVerificationEntity otpVerificationEntity : otpVerificationEntities){
            OtpVerificationDTO otpVerificationDTO = new OtpVerificationDTO();
            if(otpVerificationEntity.getUserEntity() != null){
                UserDTO userDTO = new UserDTO();
                for (RoleEntity roleEntity : otpVerificationEntity.getUserEntity().getRoleEntities()){
                    userDTO.getRole_name().add(roleEntity.getRoleName());
                }
                modelMapper.map(otpVerificationEntity.getUserEntity(), userDTO);
            }
            modelMapper.map(otpVerificationEntity, otpVerificationDTO);
            otpVerificationDTOS.add(otpVerificationDTO);
        }
        return otpVerificationDTOS;
    }
}
