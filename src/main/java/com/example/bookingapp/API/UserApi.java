package com.example.bookingapp.API;

import com.example.bookingapp.Entity.CustomerEntity;
import com.example.bookingapp.Entity.RoleEntity;
import com.example.bookingapp.Entity.UserEntity;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.LoginDTO;
import com.example.bookingapp.Models.Request.*;
import com.example.bookingapp.Models.Response.MessageResponse;
import com.example.bookingapp.Repository.CustomerRepository;
import com.example.bookingapp.Repository.UserRepository;
import com.example.bookingapp.Services.*;
import com.example.bookingapp.Utils.ConvertByteToBase64;
import com.example.bookingapp.Utils.JwtTokenUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
public class UserApi {
    @Autowired
    UserService userService;
    @Autowired
    OtpVerificationService otpVerificationService;
    @Autowired
    StatusService statusService;
    @Autowired
    MailService mailService;
    @Autowired
    CustomerService customerService;
    @Autowired
    JwtTokenUtils jwtTokenUtils;
    @Autowired
    CustomerRepository customerRepository;

    
    @Autowired
    UserRepository userRepository;
    @Autowired
    TechnicianService technicianService;

    @GetMapping("/api/me/")
    public ResponseEntity<?> getCurrentUser(@CookieValue("token") String token) {
        String email = jwtTokenUtils.getUsernameFromJWT(token);
        UserEntity user = userRepository.findByEmail(email);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        if (token == null || !jwtTokenUtils.validateToken(token, user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setMessage("Login success");
        loginDTO.setToken(token);
        loginDTO.setId_user(user.getId_user());
        loginDTO.setFull_name(user.getFull_name());
        loginDTO.setAvatarBase64(ConvertByteToBase64.toBase64(user.getAvatar()));
        for (RoleEntity roleEntity : user.getRoleEntities()){
            loginDTO.getRoles().add(roleEntity.getRoleName());
        }
        loginDTO.setHttpStatus(HttpStatus.OK);

        return ResponseEntity.ok(loginDTO);
    }

    @PostMapping(value = "/api/login/")
    @CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
    public ResponseEntity<Object> Login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        Object result = userService.login(loginRequest);
        if (result instanceof ErrorDTO) {
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        Cookie cookie = new Cookie("token", ((LoginDTO)result).getToken());
        cookie.setHttpOnly(false); // Nếu frontend cần đọc token để set Authorization header
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);
        response.addCookie(cookie);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value = "/api/register/")
    public ResponseEntity<Object> Register(@RequestBody RegisterCustomerRequest registerCustomerRequest, HttpSession session) {
        MessageResponse messageResponse = new MessageResponse();
        ErrorDTO errorDTO = new ErrorDTO();
        try {
            //Random otp code
            String otpCode = String.format("%06d", new Random().nextInt(999999));

            //Lưu user và otp vào sesstion
            session.setAttribute("user", registerCustomerRequest);
            session.setAttribute("otpCode", otpCode);
            session.setAttribute("expiry", System.currentTimeMillis() + 300000); //thời gian sống là 5p

            String emailContent = String.format(
                    "Xin chào %s,\n\n" +
                            "Mã xác thực OTP của bạn là: %s\n" +
                            "Mã này có hiệu lực trong 5 phút, vui lòng không chia sẽ mã này cho bất kì ai\n\n" +
                            "Trân trọng!\n" +
                            "From KingTech with love",
                    registerCustomerRequest.getFull_name(), otpCode
            );
            mailService.sendEmail(registerCustomerRequest.getEmail(), "Mã xác thực otp - KingTech", emailContent);
            messageResponse.setMessage("OTP code has been sent to email");
            messageResponse.setHttpStatus(HttpStatus.OK);
            return new ResponseEntity<>(messageResponse, HttpStatus.OK);
        } catch (Exception ex) {
            errorDTO.setMessage("Can not send otp code");
            errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
            return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/api/register/technician/")
    public ResponseEntity<Object> RegisterForTechnician(@RequestBody RegisterTechnicianRequest registerTechnicianRequest, HttpSession session) {
        MessageResponse messageResponse = new MessageResponse();
        ErrorDTO errorDTO = new ErrorDTO();
        try {
            //Random otp code
            String otpCode = String.format("%06d", new Random().nextInt(999999));

            //Lưu user và otp vào sesstion
            session.setAttribute("technician", registerTechnicianRequest);
            session.setAttribute("otpCode", otpCode);
            session.setAttribute("expiry", System.currentTimeMillis() + 300000); //thời gian sống là 5p

            String emailContent = String.format(
                    "Xin chào %s,\n\n" +
                            "Mã xác thực OTP của bạn là: %s\n" +
                            "Mã này có hiệu lực trong 5 phút, vui lòng không chia sẽ mã này cho bất kì ai\n\n" +
                            "Trân trọng!\n" +
                            "From KingTech with love",
                    registerTechnicianRequest.getFull_name(), otpCode
            );
            mailService.sendEmail(registerTechnicianRequest.getEmail(), "Mã xác thực otp - KingTech", emailContent);
            messageResponse.setMessage("OTP code has been sent to email");
            messageResponse.setHttpStatus(HttpStatus.OK);
            return new ResponseEntity<>(messageResponse, HttpStatus.OK);
        } catch (Exception ex) {
            errorDTO.setMessage("Can not send otp code");
            errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
            return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/api/forgotpassword/")
    public ResponseEntity<Object> forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest, HttpSession session){
        MessageResponse messageResponse = new MessageResponse();
        ErrorDTO errorDTO = new ErrorDTO();
        try {
            //Random otp code
            String otpCode = String.format("%06d", new Random().nextInt(999999));

            //Lưu user và otp vào sesstion
            session.setAttribute("forgotPassword", forgotPasswordRequest);
            session.setAttribute("otpCode", otpCode);
            session.setAttribute("expiry", System.currentTimeMillis() + 300000); //thời gian sống là 5p

            String emailContent = String.format(
                    "Xin chào %s,\n\n" +
                            "Mã xác thực OTP để khôi phục mật khẩu của bạn là: %s\n" +
                            "Mã này có hiệu lực trong 5 phút, vui lòng không chia sẽ mã này cho bất kì ai\n\n" +
                            "Trân trọng!\n" +
                            "From KingTech app with love",
                        userService.findByEmail(forgotPasswordRequest.getEmail()).getFull_name()
                        , otpCode
            );
            mailService.sendEmail(forgotPasswordRequest.getEmail(), "Mã xác thực otp - KingTech", emailContent);
            messageResponse.setMessage("OTP code has been sent to email");
            messageResponse.setHttpStatus(HttpStatus.OK);
            return new ResponseEntity<>(messageResponse, HttpStatus.OK);
        } catch (Exception ex) {
            errorDTO.setMessage("Can not send otp code");
            errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
            return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/api/changepassword/")
    public ResponseEntity<Object> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest, HttpSession session){
        MessageResponse messageResponse = new MessageResponse();
        ErrorDTO errorDTO = new ErrorDTO();
        try {
            //Random otp code
            String otpCode = String.format("%06d", new Random().nextInt(999999));

            //Lưu user và otp vào sesstion
            session.setAttribute("changePassword", changePasswordRequest);
            session.setAttribute("otpCode", otpCode);
            session.setAttribute("expiry", System.currentTimeMillis() + 300000); //thời gian sống là 5p

            String emailContent = String.format(
                    "Xin chào %s,\n\n" +
                            "Mã xác thực OTP để khôi phục mật khẩu của bạn là: %s\n" +
                            "Mã này có hiệu lực trong 5 phút, vui lòng không chia sẽ mã này cho bất kì ai\n\n" +
                            "Trân trọng!\n" +
                            "From KingTech with love",
                    userService.findByEmail(changePasswordRequest.getEmail()).getFull_name()
                    , otpCode
            );
            mailService.sendEmail(changePasswordRequest.getEmail(), "Mã xác thực otp - KingTech", emailContent);
            messageResponse.setMessage("OTP code has been sent to email");
            messageResponse.setHttpStatus(HttpStatus.OK);
            return new ResponseEntity<>(messageResponse, HttpStatus.OK);
        } catch (Exception ex) {
            errorDTO.setMessage("Can not send otp code");
            errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
            return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(value = "/api/customer/email")
    public ResponseEntity<Object> updateEmailForCustomer(@RequestBody UpdateEmailRequest updateEmailRequest, HttpSession session){
        MessageResponse messageResponse = new MessageResponse();
        ErrorDTO errorDTO = new ErrorDTO();
        try {
            //Random otp code
            String otpCode = String.format("%06d", new Random().nextInt(999999));

            //Lưu user và otp vào sesstion
            session.setAttribute("updateEmail", updateEmailRequest);
            session.setAttribute("otpCode", otpCode);
            session.setAttribute("expiry", System.currentTimeMillis() + 300000); //thời gian sống là 5p

            String emailContent = String.format(
                    "Xin chào %s,\n\n" +
                            "Mã xác thực OTP để cập nhật email của bạn là: %s\n" +
                            "Mã này có hiệu lực trong 5 phút, vui lòng không chia sẽ mã này cho bất kì ai\n\n" +
                            "Trân trọng!\n" +
                            "From KingTech with love",
                    userService.findByEmail(updateEmailRequest.getOld_email()).getFull_name()
                    , otpCode
            );
            mailService.sendEmail(updateEmailRequest.getOld_email(), "Mã xác thực otp - KingTech", emailContent);
            messageResponse.setMessage("OTP code has been sent to email");
            messageResponse.setHttpStatus(HttpStatus.OK);
            return new ResponseEntity<>(messageResponse, HttpStatus.OK);
        } catch (Exception ex) {
            errorDTO.setMessage("Can not send otp code");
            errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
            return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/api/verify-otp/")
    public ResponseEntity<Object> verifyOtp(@RequestBody Map<String, String> body, HttpSession session) {
        OtpVerificationRequest otpVerificationRequest = new OtpVerificationRequest();
        ErrorDTO errorDTO = new ErrorDTO();
        String email = null;
        //lấy otp từ người dùng truyền vào
        String otp = body.get("otp");
        //lấy otp từ session
        String sessionOtp = (String) session.getAttribute("otpCode");
        //set otp cho otprequest để lưu lại
        otpVerificationRequest.setOtpCode(sessionOtp);
        //Thời hạn của otp
        Long otpExpiry = (Long) session.getAttribute("expiry");
        //thông tin đăng kí của khách hàng
        RegisterCustomerRequest registerCustomerRequest = (RegisterCustomerRequest) session.getAttribute("user");
        //thông tin đăng kí của thợ
        RegisterTechnicianRequest registerTechnicianRequest = (RegisterTechnicianRequest) session.getAttribute("technician");
        //thông tin khi người dùng quên mật khẩu
        ForgotPasswordRequest forgotPasswordRequest = (ForgotPasswordRequest) session.getAttribute("forgotPassword");
        //thông tin người dùng khi thay đổi mật khẩu
        ChangePasswordRequest changePasswordRequest = (ChangePasswordRequest) session.getAttribute("changePassword");
        //Thông tin email của người dùng khi thay đổi email
        UpdateEmailRequest updateEmailRequest = (UpdateEmailRequest) session.getAttribute("updateEmail");

        //kiểm tra rỗng
        if (sessionOtp == null || otpExpiry == null) {
            errorDTO.setMessage("One of the session fields is empty.");
            errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
            return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
        }
        //kiểm tra hạn của session
        if (System.currentTimeMillis() > otpExpiry) {
            errorDTO.setMessage("OTP has expired, please resend a new OTP code.");
            errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
            otpVerificationRequest.setName_status("EXPIRED");
            return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
        }

        //so sánh otp người dùng gửi và otp của trên session đã lưu trước đó
        Object result = null;
        if (sessionOtp.equals(otp)) {

            //Kiểm tra xem người dùng thực hiện cái nào thì cái đó sẽ khác null
            if(registerTechnicianRequest != null){
                email = registerTechnicianRequest.getEmail();
                result = userService.registerForTechnician(registerTechnicianRequest);
            }

            if (registerCustomerRequest != null){
                email = registerCustomerRequest.getEmail();
                result = userService.registerForCustomer(registerCustomerRequest);
            }

            if (forgotPasswordRequest != null){
                email = forgotPasswordRequest.getEmail();
                result = userService.forgotPassword(forgotPasswordRequest);
            }

            if (changePasswordRequest != null){
                email = changePasswordRequest.getEmail();
                result = userService.changePassword(changePasswordRequest);
            }

            if (updateEmailRequest != null){
                result =  customerService.updateEmail(updateEmailRequest);
                //sau khi cập nhật xong thì tiến hành lấy email mới
                email = updateEmailRequest.getNew_email();
            }


            if (result instanceof ErrorDTO) {
                return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
            }
            //set thông tin cho otp request
            otpVerificationRequest.setExpires_at(LocalDateTime.now());
            otpVerificationRequest.setName_status("VERIFIED");
            //lấy ra id của user thông qua email khi đăng kí thành công
            otpVerificationRequest.setId_user(userService.findByEmail(email).getId_user());
            otpVerificationService.saveOtp(otpVerificationRequest);
            //xóa otp code ra khỏi session
            session.removeAttribute("otpcode");
            session.removeAttribute("expiry");
            if(registerTechnicianRequest != null){
                session.removeAttribute("technician");
            }
            if(registerCustomerRequest != null){
                session.removeAttribute("user");
            }
            if (forgotPasswordRequest != null){
                session.removeAttribute("forgotPassword");
            }
            if (changePasswordRequest != null){
                session.removeAttribute("changePassword");
            }
            if (updateEmailRequest != null){
                session.removeAttribute("updateEmail");
            }
        }else{
            MessageResponse response = new MessageResponse();
            response.setHttpStatus(HttpStatus.BAD_REQUEST);
            response.setMessage("OTP không chính xác");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value = "/api/resend-otp/")
    public ResponseEntity<Object> resendOtp(HttpSession session) {
        MessageResponse messageResponse = new MessageResponse();
        ErrorDTO errorDTO = new ErrorDTO();
        try {
            RegisterCustomerRequest registerCustomerRequest = (RegisterCustomerRequest) session.getAttribute("user");
            if (registerCustomerRequest == null) {
                messageResponse.setMessage("Session expired");
                messageResponse.setHttpStatus(HttpStatus.BAD_REQUEST);
                return new ResponseEntity<>(messageResponse, HttpStatus.BAD_REQUEST);
            }

            //tạo lại mã otp mới
            String otpCode = String.format("%06d", new Random().nextInt(999999));

            //cập nhật lại otp
            session.setAttribute("otpCode", otpCode);
            session.setAttribute("expiry", System.currentTimeMillis() + 300000); //thời gian sống là 5p

            String emailContent = String.format(
                    "Xin chào %s,\n\n" +
                            "Mã xác thực OTP của bạn là: %s\n" +
                            "Mã này có hiệu lực trong 5 phút, vui lòng không chia sẽ mã này cho bất kì ai\n\n" +
                            "Trân trọng!\n" +
                            "From KingTech with love",
                    registerCustomerRequest.getFull_name(), otpCode
            );
            mailService.sendEmail(registerCustomerRequest.getEmail(), "Mã xác thực otp - KingTech", emailContent);
            messageResponse.setMessage("OTP code has been sent to email");
            messageResponse.setHttpStatus(HttpStatus.OK);
            return new ResponseEntity<>(messageResponse, HttpStatus.OK);
        } catch (Exception ex) {
            errorDTO.setMessage("Can not send otp code");
            errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
            return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
        }
    }

    //client lưu token được trả về trên session/cookie/localstorage
    //khi logout client sẽ xóa token ra khỏi nơi lưu là được
    @PostMapping("/api/logout/")
    public ResponseEntity<Object> logout() {
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setMessage("Logout success");
        messageResponse.setHttpStatus(HttpStatus.OK);
        return new ResponseEntity<>(messageResponse, HttpStatus.OK);
    }

}
