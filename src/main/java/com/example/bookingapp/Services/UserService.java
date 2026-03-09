package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.UserDTO;
import com.example.bookingapp.Models.Request.*;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    Object login(LoginRequest loginRequest);
    Object registerForCustomer(RegisterCustomerRequest registerCustomerRequest);
    Object registerForTechnician(RegisterTechnicianRequest registerTechnicianRequest);
    UserDTO findByEmail(String email);
    Object forgotPassword(ForgotPasswordRequest forgotPasswordRequest);
    Object changePassword(ChangePasswordRequest changePasswordRequest);
    Object updateEmail(UpdateEmailRequest updateEmailRequest);
}
