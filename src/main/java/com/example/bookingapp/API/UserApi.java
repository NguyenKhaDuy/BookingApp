package com.example.bookingapp.API;

import com.example.bookingapp.Entity.CustomerEntity;
import com.example.bookingapp.Entity.RoleEntity;
import com.example.bookingapp.Entity.UserEntity;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.LoginDTO;
import com.example.bookingapp.Models.DTO.OtpVerificationDTO;
import com.example.bookingapp.Models.DTO.UserDTO;
import com.example.bookingapp.Models.Request.*;
import com.example.bookingapp.Models.Response.MessageResponse;
import com.example.bookingapp.Repository.CustomerRepository;
import com.example.bookingapp.Repository.RoleRepository;
import com.example.bookingapp.Repository.UserRepository;
import com.example.bookingapp.Services.*;
import com.example.bookingapp.Utils.ConvertByteToBase64;
import com.example.bookingapp.Utils.JwtTokenUtils;
import com.example.bookingapp.Utils.RandomIdUtils;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
public class UserApi {
    //test git
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
    @Value("${GOOGLE_CLIENT_ID}")
    private String googleClientId;

    private Object object = null;
    @Autowired
    private RoleRepository roleRepository;

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
        for (RoleEntity roleEntity : user.getRoleEntities()) {
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
        Cookie cookie = new Cookie("token", ((LoginDTO) result).getToken());
        cookie.setHttpOnly(false); // Nếu frontend cần đọc token để set Authorization header
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);
        response.addCookie(cookie);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

//    @PostMapping(value = "/api/register/")
//    public ResponseEntity<Object> Register(@RequestBody RegisterCustomerRequest registerCustomerRequest, HttpSession session) {
//        MessageResponse messageResponse = new MessageResponse();
//        ErrorDTO errorDTO = new ErrorDTO();
//        try {
//            //Random otp code
//            String otpCode = String.format("%06d", new Random().nextInt(999999));
//
//            //Lưu user và otp vào sesstion
//            session.setAttribute("user", registerCustomerRequest);
//            session.setAttribute("otpCode", otpCode);
//            session.setAttribute("expiry", System.currentTimeMillis() + 300000); //thời gian sống là 5p
//
//            String emailContent = String.format(
//                    "Xin chào %s,\n\n" +
//                            "Mã xác thực OTP của bạn là: %s\n" +
//                            "Mã này có hiệu lực trong 5 phút, vui lòng không chia sẽ mã này cho bất kì ai\n\n" +
//                            "Trân trọng!\n" +
//                            "From KingTech with love",
//                    registerCustomerRequest.getFull_name(), otpCode
//            );
//            mailService.sendEmail(registerCustomerRequest.getEmail(), "Mã xác thực otp - KingTech", emailContent);
//            messageResponse.setMessage("OTP code has been sent to email");
//            messageResponse.setHttpStatus(HttpStatus.OK);
//            return new ResponseEntity<>(messageResponse, HttpStatus.OK);
//        } catch (Exception ex) {
//            errorDTO.setMessage("Can not send otp code");
//            errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
//            return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
//        }
//    }

    @PostMapping(value = "/api/register/")
    public ResponseEntity<Object> Register(@RequestBody RegisterCustomerRequest registerCustomerRequest) {
        MessageResponse messageResponse = new MessageResponse();
        ErrorDTO errorDTO = new ErrorDTO();
        try {
            //Random otp code
            String otpCode = String.format("%06d", new Random().nextInt(999999));

            String emailContent = String.format(
                    "Xin chào %s,\n\n" +
                            "Mã xác thực OTP của bạn là: %s\n" +
                            "Mã này có hiệu lực trong 5 phút, vui lòng không chia sẽ mã này cho bất kì ai\n\n" +
                            "Trân trọng!\n" +
                            "From KingTech with love",
                    registerCustomerRequest.getFull_name(), otpCode
            );
            mailService.sendEmail(registerCustomerRequest.getEmail(), "Mã xác thực otp - KingTech", emailContent);

            //gán cho object biến toàn cục
            object = registerCustomerRequest;


            //Luu otp vao database
            OtpVerificationRequest otpVerificationRequest = new OtpVerificationRequest();
            otpVerificationRequest.setExpires_at(LocalDateTime.now().plusMinutes(5));
            otpVerificationRequest.setName_status("UNVERIFIED");
            otpVerificationRequest.setEmail(registerCustomerRequest.getEmail());
            //lấy ra id của user thông qua email khi đăng kí thành công
            otpVerificationRequest.setOtpCode(otpCode);
            otpVerificationRequest.setId_user(null);

            otpVerificationService.saveOtp(otpVerificationRequest);

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
    public ResponseEntity<Object> verifyOtp(@RequestBody Map<String, String> body) {

        OtpVerificationRequest otpVerificationRequest = new OtpVerificationRequest();
        ErrorDTO errorDTO = new ErrorDTO();
        String email = body.get("email");
        String type = body.get("type");
        //lấy otp từ người dùng truyền vào
        String otp = body.get("otp");
        //lấy từ database lên và lấy otp mới nhất
        OtpVerificationDTO otpVerificationDTO = otpVerificationService.getByEmail(email).get(0);
        String otpDatabse = otpVerificationDTO.getOtp_code();

        //kiểm tra hạn của otp
        if (otpVerificationDTO.getExpires_at().isBefore(LocalDateTime.now())) {
            errorDTO.setMessage("OTP has expired, please resend a new OTP code.");
            errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
            otpVerificationRequest.setName_status("EXPIRED");
            return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
        }
//        //so sánh otp người dùng gửi và otp của trên database đã lưu trước đó
        Object result = null;
        if (otpDatabse.equals(otp)) {

//            //Kiểm tra xem người dùng thực hiện cái nào thì cái đó sẽ khác null
//            if(registerTechnicianRequest != null){
//                email = registerTechnicianRequest.getEmail();
//                result = userService.registerForTechnician(registerTechnicianRequest);
//            }

            if (type.equals("REGISTER")) {
                result = userService.registerForCustomer((RegisterCustomerRequest) object);
                email = ((RegisterCustomerRequest) object).getEmail();
            }

            if (type.equals("FORGOT_PASSWORD")) {
                result = userService.forgotPassword((ForgotPasswordRequest) object);
                email = ((ForgotPasswordRequest) object).getEmail();
            }

            if (type.equals("CHANGE_PASSWORD")) {
                result = userService.changePassword((ChangePasswordRequest) object);
                email = ((ChangePasswordRequest) object).getEmail();
            }

            if (type.equals("UPDATE_EMAIL")) {
                result = userService.updateEmail((UpdateEmailRequest) object);
                //sau khi cập nhật xong thì tiến hành lấy email mới
                email = ((UpdateEmailRequest) object).getNew_email();
            }

            if (result instanceof ErrorDTO) {
                return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
            }
            //set thông tin cho otp request
            otpVerificationRequest.setName_status("VERIFIED");
            //lấy ra id của user thông qua email khi đăng kí thành công
            otpVerificationRequest.setId_user(userService.findByEmail(email).getId_user());
            otpVerificationService.saveOtp(otpVerificationRequest);

        } else {
            MessageResponse response = new MessageResponse();
            response.setHttpStatus(HttpStatus.BAD_REQUEST);
            response.setMessage("OTP không chính xác");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
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

    @PostMapping(value = "/api/forgotpassword/send-otp/")
    public ResponseEntity<Object> forgotPasswordSendOtp(@RequestParam String email) {
        MessageResponse messageResponse = new MessageResponse();
        ErrorDTO errorDTO = new ErrorDTO();
        UserDTO userDTO = userService.findByEmail(email);
        try {
            //Random otp code
            String otpCode = String.format("%06d", new Random().nextInt(999999));

            //Luu otp vao database
            OtpVerificationRequest otpVerificationRequest = new OtpVerificationRequest();
            otpVerificationRequest.setExpires_at(LocalDateTime.now().plusMinutes(5));
            otpVerificationRequest.setName_status("UNVERIFIED");
            otpVerificationRequest.setEmail(email);
            //lấy ra id của user thông qua email khi đăng kí thành công
            otpVerificationRequest.setOtpCode(otpCode);
            otpVerificationRequest.setId_user(userDTO.getId_user());
            otpVerificationService.saveOtp(otpVerificationRequest);

            String emailContent = String.format(
                    "Xin chào %s,\n\n" +
                            "Mã xác thực OTP để khôi phục mật khẩu của bạn là: %s\n" +
                            "Mã này có hiệu lực trong 5 phút, vui lòng không chia sẽ mã này cho bất kì ai\n\n" +
                            "Trân trọng!\n" +
                            "From KingTech app with love",
                    userService.findByEmail(email).getFull_name()
                    , otpCode
            );
            mailService.sendEmail(email, "Mã xác thực otp - KingTech", emailContent);
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
    public ResponseEntity<Object> forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        Object result = userService.forgotPassword(forgotPasswordRequest);
        object = forgotPasswordRequest;
        if (result instanceof ErrorDTO) {
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    @PostMapping(value = "/api/changepassword/")
    public ResponseEntity<Object> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        MessageResponse messageResponse = new MessageResponse();
        ErrorDTO errorDTO = new ErrorDTO();
        object = changePasswordRequest;
        UserDTO userDTO = userService.findByEmail(changePasswordRequest.getEmail());
        try {
            //Random otp code
            String otpCode = String.format("%06d", new Random().nextInt(999999));

            //Luu otp vao database
            OtpVerificationRequest otpVerificationRequest = new OtpVerificationRequest();
            otpVerificationRequest.setExpires_at(LocalDateTime.now().plusMinutes(5));
            otpVerificationRequest.setName_status("UNVERIFIED");
            otpVerificationRequest.setEmail(changePasswordRequest.getEmail());
            //lấy ra id của user thông qua email khi đăng kí thành công
            otpVerificationRequest.setOtpCode(otpCode);
            otpVerificationRequest.setId_user(userDTO.getId_user());
            otpVerificationService.saveOtp(otpVerificationRequest);

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

    @PutMapping(value = "/api/email")
    public ResponseEntity<Object> updateEmail(@RequestBody UpdateEmailRequest updateEmailRequest) {
        MessageResponse messageResponse = new MessageResponse();
        ErrorDTO errorDTO = new ErrorDTO();
        UserDTO userDTO = userService.findByEmail(updateEmailRequest.getOld_email());
        try {
            //Random otp code
            String otpCode = String.format("%06d", new Random().nextInt(999999));

            //Luu otp vao database
            OtpVerificationRequest otpVerificationRequest = new OtpVerificationRequest();
            otpVerificationRequest.setExpires_at(LocalDateTime.now().plusMinutes(5));
            otpVerificationRequest.setName_status("UNVERIFIED");
            otpVerificationRequest.setEmail(updateEmailRequest.getOld_email());
            //lấy ra id của user thông qua email khi đăng kí thành công
            otpVerificationRequest.setOtpCode(otpCode);
            otpVerificationRequest.setId_user(userDTO.getId_user());
            otpVerificationService.saveOtp(otpVerificationRequest);

            object = updateEmailRequest;

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

//    @PostMapping(value = "/api/verify-otp/")
//    public ResponseEntity<Object> verifyOtp(@RequestBody Map<String, String> body, HttpSession session) {
//        OtpVerificationRequest otpVerificationRequest = new OtpVerificationRequest();
//        ErrorDTO errorDTO = new ErrorDTO();
//        String email = null;
//        //lấy otp từ người dùng truyền vào
//        String otp = body.get("otp");
//        //lấy otp từ session
//        String sessionOtp = (String) session.getAttribute("otpCode");
//        //set otp cho otprequest để lưu lại
//        otpVerificationRequest.setOtpCode(sessionOtp);
//        //Thời hạn của otp
//        Long otpExpiry = (Long) session.getAttribute("expiry");
//        //thông tin đăng kí của khách hàng
//        RegisterCustomerRequest registerCustomerRequest = (RegisterCustomerRequest) session.getAttribute("user");
//        //thông tin đăng kí của thợ
//        RegisterTechnicianRequest registerTechnicianRequest = (RegisterTechnicianRequest) session.getAttribute("technician");
//        //thông tin khi người dùng quên mật khẩu
//        String emailForgot = (String) session.getAttribute("forgotPassword");
//        //thông tin người dùng khi thay đổi mật khẩu
//        ChangePasswordRequest changePasswordRequest = (ChangePasswordRequest) session.getAttribute("changePassword");
//        //Thông tin email của người dùng khi thay đổi email
//        UpdateEmailRequest updateEmailRequest = (UpdateEmailRequest) session.getAttribute("updateEmail");
//
//        //kiểm tra rỗng
//        if (sessionOtp == null || otpExpiry == null) {
//            errorDTO.setMessage("One of the session fields is empty.");
//            errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
//            return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
//        }
//        //kiểm tra hạn của session
//        if (System.currentTimeMillis() > otpExpiry) {
//            errorDTO.setMessage("OTP has expired, please resend a new OTP code.");
//            errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
//            otpVerificationRequest.setName_status("EXPIRED");
//            return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
//        }
//
//        //so sánh otp người dùng gửi và otp của trên session đã lưu trước đó
//        Object result = null;
//        if (sessionOtp.equals(otp)) {
//
//            //Kiểm tra xem người dùng thực hiện cái nào thì cái đó sẽ khác null
//            if(registerTechnicianRequest != null){
//                email = registerTechnicianRequest.getEmail();
//                result = userService.registerForTechnician(registerTechnicianRequest);
//            }
//
//            if (registerCustomerRequest != null){
//                email = registerCustomerRequest.getEmail();
//                result = userService.registerForCustomer(registerCustomerRequest);
//            }
//
//            if (emailForgot != null){
//                email = emailForgot;

    /// /                result = userService.forgotPassword(forgotPasswordRequest);
//            }
//
//            if (changePasswordRequest != null){
//                email = changePasswordRequest.getEmail();
//                result = userService.changePassword(changePasswordRequest);
//            }
//
//            if (updateEmailRequest != null){
//                result =  userService.updateEmail(updateEmailRequest);
//                //sau khi cập nhật xong thì tiến hành lấy email mới
//                email = updateEmailRequest.getNew_email();
//            }
//
//
//            if (result instanceof ErrorDTO) {
//                return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
//            }
//            //set thông tin cho otp request
//            otpVerificationRequest.setExpires_at(LocalDateTime.now());
//            otpVerificationRequest.setName_status("VERIFIED");
//            //lấy ra id của user thông qua email khi đăng kí thành công
//            otpVerificationRequest.setId_user(userService.findByEmail(email).getId_user());
//            otpVerificationService.saveOtp(otpVerificationRequest);
//            //xóa otp code ra khỏi session
//            session.removeAttribute("otpcode");
//            session.removeAttribute("expiry");
//            if(registerTechnicianRequest != null){
//                session.removeAttribute("technician");
//            }
//            if(registerCustomerRequest != null){
//                session.removeAttribute("user");
//            }
//            if (emailForgot != null){
//                session.removeAttribute("forgotPassword");
//            }
//            if (changePasswordRequest != null){
//                session.removeAttribute("changePassword");
//            }
//            if (updateEmailRequest != null){
//                session.removeAttribute("updateEmail");
//            }
//        }else{
//            MessageResponse response = new MessageResponse();
//            response.setHttpStatus(HttpStatus.BAD_REQUEST);
//            response.setMessage("OTP không chính xác");
//            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//        }
//        return new ResponseEntity<>(result, HttpStatus.OK);
//    }
    @PostMapping(value = "/api/resend-otp/")
    public ResponseEntity<Object> resendOtp() {
        MessageResponse messageResponse = new MessageResponse();
        ErrorDTO errorDTO = new ErrorDTO();
        try {
            RegisterCustomerRequest registerCustomerRequest = (RegisterCustomerRequest) object;
            if (registerCustomerRequest == null) {
                messageResponse.setMessage("Session expired");
                messageResponse.setHttpStatus(HttpStatus.BAD_REQUEST);
                return new ResponseEntity<>(messageResponse, HttpStatus.BAD_REQUEST);
            }

            //tạo lại mã otp mới
            String otpCode = String.format("%06d", new Random().nextInt(999999));

            //Luu otp vao database
            OtpVerificationRequest otpVerificationRequest = new OtpVerificationRequest();
            otpVerificationRequest.setExpires_at(LocalDateTime.now().plusMinutes(5));
            otpVerificationRequest.setName_status("UNVERIFIED");
            otpVerificationRequest.setEmail(registerCustomerRequest.getEmail());
            //lấy ra id của user thông qua email khi đăng kí thành công
            otpVerificationRequest.setOtpCode(otpCode);
            otpVerificationRequest.setId_user(null);

            otpVerificationService.saveOtp(otpVerificationRequest);

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


    //dùng cho app di động
    @PostMapping("/api/auth/google-login")
    public ResponseEntity<?> loginGoogle(@RequestBody Map<String, String> body) {

        try {
            String idTokenString = body.get("token");

            if (idTokenString == null || idTokenString.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Missing Google token");
            }

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance()
            )
                    .setAudience(Collections.singletonList(
                            googleClientId
                    ))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid Google token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String avatarUrl = (String) payload.get("picture");
            byte[] avatarBytes = null;
            try (InputStream in = new URL(avatarUrl).openStream()) {
                avatarBytes = in.readAllBytes();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // tìm user
            CustomerEntity customer = customerRepository.findByEmail(email);

            // nếu chưa có → tạo mới
            if (customer == null) {
                RoleEntity role = roleRepository.findByRoleName("CUSTOMER");

                customer = new CustomerEntity();
                customer.setId_user(RandomIdUtils.generateRandomId("U", 10));
                customer.setEmail(email);
                customer.setFull_name(name);
                customer.setAvatar(avatarBytes);
                customer.setCreated_at(LocalDateTime.now());
                customer.setUpdated_at(LocalDateTime.now());
                customer.getRoleEntities().add(role);
                customerRepository.save(customer);
                role.getUserEntities().add(customer);
                roleRepository.save(role);
            }

            // generate JWT hệ thống (HS256)
            String jwt = jwtTokenUtils.generateToken(customer);

            return ResponseEntity.ok(Map.of(
                    "token", jwt,
                    "email", email,
                    "name", name,
                    "avatar", avatarUrl,
                    "roles", customer.getRoleEntities()
                            .stream()
                            .map(RoleEntity::getRoleName)
                            .toList()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Google login failed");
        }
    }

}
