package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.*;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.LoginDTO;
import com.example.bookingapp.Models.DTO.UserDTO;
import com.example.bookingapp.Models.Request.*;
import com.example.bookingapp.Models.Response.MessageResponse;
import com.example.bookingapp.Repository.*;
import com.example.bookingapp.Services.UserService;
import com.example.bookingapp.Utils.JwtTokenUtils;
import com.example.bookingapp.Utils.RandomIdUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtTokenUtils jwtTokenUtils;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    LevelRepository levelRepository;
    @Autowired
    TechnicianWalletRepository technicianWalletRepository;
    @Override
    public Object login(LoginRequest loginRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        LoginDTO loginDTO = new LoginDTO();
        try{
            UserEntity userEntity = userRepository.findByEmail(loginRequest.getEmail());
            if (userEntity != null){
                if (!passwordEncoder.matches(loginRequest.getPassword(), userEntity.getPassword())){
                    errorDTO.setMessage("Password incorrect");
                    errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
                    return errorDTO;
                }
            }
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword(), userEntity.getAuthorities());
            authenticationManager.authenticate(authenticationToken);
            String token = jwtTokenUtils.generateToken(userEntity);
            loginDTO.setMessage("Login success");
            loginDTO.setToken(token);
            loginDTO.setHttpStatus(HttpStatus.OK);
            return loginDTO;
        }catch (NullPointerException ex){
            errorDTO.setMessage("Can not found email");
            errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
            return errorDTO;
        }
    }

    @Override
    public Object registerForCustomer(RegisterCustomerRequest registerCustomerRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageResponse messageResponse = new MessageResponse();
        UserEntity userEntity = userRepository.findByEmail(registerCustomerRequest.getEmail());
        if (userEntity != null){
            errorDTO.setMessage("Email already exists");
            errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
            return errorDTO;
        }
        CustomerEntity customerEntity = new CustomerEntity();
        modelMapper.map(registerCustomerRequest, customerEntity);
        customerEntity.setId_user(RandomIdUtils.generateRandomId("U", 10));
        customerEntity.setPassword(passwordEncoder.encode(registerCustomerRequest.getPassword()));
        try {
            RoleEntity roleEntity = roleRepository.findByRoleName("CUSTOMER");
            //set role cho người dùng
            customerEntity.getRoleEntities().add(roleEntity);
            customerEntity.setCreated_at(LocalDateTime.now());
            customerEntity.setUpdated_at(LocalDateTime.now());
            userRepository.save(customerEntity);
            roleEntity.getUserEntities().add(customerEntity);
            roleRepository.save(roleEntity);
            messageResponse.setMessage("Register success");
            messageResponse.setHttpStatus(HttpStatus.OK);
            return messageResponse;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found role");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object registerForTechnician(RegisterTechnicianRequest registerTechnicianRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageResponse messageResponse = new MessageResponse();
        UserEntity userEntity = userRepository.findByEmail(registerTechnicianRequest.getEmail());
        if (userEntity != null){
            errorDTO.setMessage("Email already exists");
            errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
            return errorDTO;
        }
        TechnicianEntity technicianEntity = new TechnicianEntity();
        modelMapper.map(registerTechnicianRequest, technicianEntity);
        technicianEntity.setId_user(RandomIdUtils.generateRandomId("U", 10));
        technicianEntity.setPassword(passwordEncoder.encode(registerTechnicianRequest.getPassword()));
        try {
            RoleEntity roleEntity = roleRepository.findByRoleName("TECHNICIAN");
            LevelEntity levelEntity = levelRepository.findByLevel("Junior");
            //set role cho người dùng
            technicianEntity.getRoleEntities().add(roleEntity);
            technicianEntity.setTechnician_debt(0);
            technicianEntity.setEfficiency(10L);
            technicianEntity.setLevelEntity(levelEntity);
            technicianEntity.setExperience_year(registerTechnicianRequest.getExperience_year());
            technicianEntity.setWorking_area(registerTechnicianRequest.getWorking_area());
            technicianEntity.setCreated_at(LocalDateTime.now());
            technicianEntity.setUpdated_at(LocalDateTime.now());
            userRepository.save(technicianEntity);

            //tạo ví điện tử cho thợ
            TechnicianWalletEntity technicianWalletEntity = new TechnicianWalletEntity();
            technicianWalletEntity.setTechnicianEntity(technicianEntity);
            technicianWalletEntity.setBalance(0);
            //thợ tự set code để rút tiền và liên kết ngân hàng
            technicianWalletRepository.save(technicianWalletEntity);

            //set role cho thợ
            roleEntity.getUserEntities().add(technicianEntity);
            roleRepository.save(roleEntity);
            messageResponse.setMessage("Register success");
            messageResponse.setHttpStatus(HttpStatus.OK);
            return messageResponse;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found role");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public UserDTO findByEmail(String email) {
        try {
            UserEntity user = userRepository.findByEmail(email);
            UserDTO userDTO = new UserDTO();
            modelMapper.map(user, userDTO);
            return userDTO;
        }catch (NoSuchElementException ex){
           return null;
        }
    }

    @Override
    public Object forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        try {
            UserEntity userEntity = userRepository.findByEmail(forgotPasswordRequest.getEmail());
            userEntity.setPassword(passwordEncoder.encode(forgotPasswordRequest.getNew_password()));
            userEntity.setUpdated_at(LocalDateTime.now());
            userRepository.save(userEntity);
            MessageResponse messageResponse = new MessageResponse();
            messageResponse.setMessage("Password updated successfully");
            messageResponse.setHttpStatus(HttpStatus.OK);
            return messageResponse;
        }catch (NoSuchElementException ex){
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setMessage("Can not found user");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object changePassword(ChangePasswordRequest changePasswordRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        try {
            UserEntity userEntity = userRepository.findByEmail(changePasswordRequest.getEmail());
            if (passwordEncoder.matches(userEntity.getPassword(), changePasswordRequest.getOld_password())){
                errorDTO.setMessage("Password incorrect");
                errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
                return errorDTO;
            }
            userEntity.setPassword(passwordEncoder.encode(changePasswordRequest.getNew_password()));
            userRepository.save(userEntity);
            MessageResponse messageResponse = new MessageResponse();
            messageResponse.setHttpStatus(HttpStatus.OK);
            messageResponse.setMessage("Change password success");
            return messageResponse;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found user");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }
}
