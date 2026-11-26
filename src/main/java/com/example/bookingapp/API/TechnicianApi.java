package com.example.bookingapp.API;

import com.example.bookingapp.Config.VnPayConfig;
import com.example.bookingapp.Entity.TechnicianEntity;
import com.example.bookingapp.Models.DTO.*;
import com.example.bookingapp.Models.Request.*;
import com.example.bookingapp.Models.Response.MessageResponse;
import com.example.bookingapp.Services.*;
import com.example.bookingapp.Utils.RandomIdUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@RestController
public class TechnicianApi {
    @Autowired
    TechnicianService technicianService;
    @Autowired
    RepairRequestService repairRequestService;
    @Autowired
    StatisticService statisticService;
    @Autowired
    MailService mailService;
    @Autowired
    UserService userService;
    @GetMapping(value = "/api/all/technician/")
    public ResponseEntity<DataDTO> getAll(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo){
        Page<TechnicicanDTO> technicicanDTOS = technicianService.getAll(pageNo);
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setTotal_page(technicicanDTOS.getTotalPages());
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setData(technicicanDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/api/technician/profile/id={id_technician}")
    public ResponseEntity<Object> getProfile(@PathVariable String id_technician){
        Object result = technicianService.getById(id_technician);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO) result).getHttpStatus());
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setData(result);
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/api/technician/searchlocation")
    public ResponseEntity<DataDTO> getAll(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestBody SearchByLocationRequest searchByLocationRequest){
        Page<TechnicicanDTO> technicicanDTOS = technicianService.searchTechnicianByLocation(pageNo, searchByLocationRequest);
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setTotal_page(technicicanDTOS.getTotalPages());
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setData(technicicanDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/api/outstanding/technician/")
    public ResponseEntity<DataDTO> getOutstandingTechnicians(){
        List<TechnicicanDTO> technicicanDTOS = technicianService.getOutstandingTechnicians();
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setData(technicicanDTOS);
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/api/technician/id={id_technician}")
    public ResponseEntity<Object> getById(@PathVariable String id_technician){
        Object result = technicianService.getById(id_technician);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO) result).getHttpStatus());
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setData(result);
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/api/detail-technician/id={id_technician}")
    public ResponseEntity<Object> detailTechnicianForCustomer(@PathVariable String id_technician){
        Object result = technicianService.getById(id_technician);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO) result).getHttpStatus());
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setData(result);
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/api/technician/searchName/")
    public ResponseEntity<DataDTO> searchByName(@RequestParam(value = "name_technician") String name_technician,
                                                @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo){
        Page<TechnicicanDTO> technicicanDTOS = technicianService.searchTechnicianByName(pageNo, name_technician);
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setTotal_page(technicicanDTOS.getTotalPages());
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setData(technicicanDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/api/technician/searchService/")
    public ResponseEntity<Object> searchByService(@RequestParam(value = "id_service") String id_service,
                                                @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo){
        Page<TechnicicanDTO> technicicanDTOS = technicianService.searchTechnicianByService(pageNo, id_service);
        if (technicicanDTOS == null){
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setMessage("Can not found service");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setTotal_page(technicicanDTOS.getTotalPages());
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setData(technicicanDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @PutMapping(value = "/api/technician/profile/")
    public ResponseEntity<Object> updateProfile(@RequestBody TechnicianProfileRequest technicianProfileRequest){
        Object result = technicianService.updateProfile(technicianProfileRequest);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping(value = "/api/technician/profile/avatar/")
    public ResponseEntity<Object> updateAvatar(@ModelAttribute AvatarRequest avatarRequest){
        Object result = technicianService.updateAvatar(avatarRequest);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping(value = "/api/technician/profile/skill/")
    public ResponseEntity<Object> addSkill(@RequestBody SkillTechnicianRequest skillTechnicianRequest){
        Object result = technicianService.addSkill(skillTechnicianRequest);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(value = "/api/technician/profile/skill/id={id_user}")
    public ResponseEntity<Object> getSkill(@PathVariable String id_user){
        List<SkillDTO> skillDTOS = technicianService.getSkill(id_user);
        if (skillDTOS == null){
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setMessage("Can not found technician");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setData(skillDTOS);
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @DeleteMapping(value = "/api/technician/profile/skill/")
    public ResponseEntity<Object> deleteSkill(@RequestBody SkillTechnicianRequest skillTechnicianRequest){
        Object result = technicianService.deleteSkillOfTechnician(skillTechnicianRequest);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value = "/api/technician/profile/location/")
    public ResponseEntity<Object> addLocation(@RequestBody LocationTechnicianRequest locationTechnicianRequest){
        Object result = technicianService.addLocation(locationTechnicianRequest);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(value = "/api/technician/profile/location/id={id_user}")
    public ResponseEntity<Object> getLocation(@PathVariable String id_user){
        List<LocationDTO> locationDTOS = technicianService.getLocation(id_user);
        if (locationDTOS == null){
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setMessage("Can not found technician");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setData(locationDTOS);
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @DeleteMapping(value = "/api/technician/profile/location/")
    public ResponseEntity<Object> deleteLocation(@RequestBody LocationTechnicianRequest locationTechnicianRequest){
        Object result = technicianService.deleteLocationOfTechnician(locationTechnicianRequest);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(value = "/api/technician/request/id_tech={id_tech}")
    public ResponseEntity<Object> getRequestByStatus(@PathVariable String id_tech, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo){
        ErrorDTO errorDTO = new ErrorDTO();
        Page<RepairRequestDTO> repairRequestDTOS = repairRequestService.getByTechnician(pageNo, id_tech);
        if(repairRequestDTOS == null){
            errorDTO.setMessage("Can not found status or technician");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setTotal_page(repairRequestDTOS.getTotalPages());
        dataDTO.setData(repairRequestDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @PutMapping(value = "/api/technician/accept-request/")
    public ResponseEntity<Object> acceptRequest(@RequestBody AcceptRequest acceptRequest){
        Object result = repairRequestService.acceptRequest(acceptRequest);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping(value = "/api/technician/refuse-request/")
    public ResponseEntity<Object> refuseRequest(@RequestBody AcceptRequest acceptRequest){
        Object result = repairRequestService.refuseRequest(acceptRequest.getId_technician(), acceptRequest.getId_request());
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/api/admin/technician/id={id_technician}")
    public ResponseEntity<Object> deleteTechnician(@PathVariable String id_technician){
        Object result = technicianService.deleteTechnician(id_technician);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO)result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping(value = "/api/technician/profile/service/")
    public ResponseEntity<Object> addService(@RequestBody ServiceTechnicianRequest serviceTechnicianRequest){
        Object result = technicianService.addService(serviceTechnicianRequest);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(value = "/api/technician/profile/service/id={id_user}")
    public ResponseEntity<Object> getService(@PathVariable String id_user){
        List<ServiceDTO> serviceDTOS = technicianService.getServices(id_user);
        if (serviceDTOS == null){
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setMessage("Can not found technician");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setData(serviceDTOS);
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @DeleteMapping(value = "/api/technician/profile/service/")
    public ResponseEntity<Object> deleteService(@RequestBody ServiceTechnicianRequest serviceTechnicianRequest){
        Object result = technicianService.deleteServiceOfTechnician(serviceTechnicianRequest);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value = "/api/admin/register/technician/")
    public ResponseEntity<Object> RegisterForTechnician(@RequestBody RegisterTechnicianRequest registerTechnicianRequest) {
        MessageResponse messageResponse = new MessageResponse();
        ErrorDTO errorDTO = new ErrorDTO();
        try {
            //random password
            String password  = RandomIdUtils.generateRandomPassword(5);
            registerTechnicianRequest.setPassword(password);
            Object result = userService.registerForTechnician(registerTechnicianRequest);

            if(result instanceof ErrorDTO){
                errorDTO = (ErrorDTO) result;
                errorDTO.setMessage("Can not create acccount for technician");
                errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
                return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
            }

            String emailContent = String.format(
                    "Xin chào %s,\n\n" +
                            "Mật khẩu tài khoản của bạn là: %s\n" +
                            "Vui lòng thay đổi mật khẩu khi đăng nhập thành công để đảm bảo tính bảo mật\n\n" +
                            "Trân trọng!\n" +
                            "From KingTech with love",
                    registerTechnicianRequest.getFull_name(), password
            );
            mailService.sendEmail(registerTechnicianRequest.getEmail(), "Mật khẩu tài khoản - KingTech", emailContent);

            messageResponse.setMessage("Password has been sent to email");
            messageResponse.setHttpStatus(HttpStatus.OK);
            return new ResponseEntity<>(messageResponse, HttpStatus.OK);
        } catch (Exception ex) {
            errorDTO.setMessage("Server error");
            errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
            return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/api/technician/payment/pay")
    public ResponseEntity<Object> payDebt(@RequestBody PaymentDebtByKingTechPayRequest paymentDebtByKingTechPayRequest) {
        Object result = technicianService.paymentDebt(paymentDebtByKingTechPayRequest);
        String deepLink;
        if (result instanceof ErrorDTO) {
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }

        if (result instanceof MessageResponse) {
            if (((MessageResponse) result).getHttpStatus().equals(HttpStatus.OK)) {
                deepLink = "kingtech://payment-debt/success";
            }else{
                deepLink = "kingtech://payment-debt/failed";
            }
        }

        return new ResponseEntity<>(result, ((MessageResponse) result).getHttpStatus());
    }

    @PostMapping("/api/technician/payment/debt/")
    public String PaymentDebt(
            @RequestBody PaymentDebtRequest paymentDebtRequest
    ) throws UnsupportedEncodingException {

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";

        Long amount = paymentDebtRequest.getAmount().longValue() * 100;

        String bankCode = paymentDebtRequest.getBank();

        String idTechnician = paymentDebtRequest.getId_technician();

        // Mã giao dịch
        String vnp_TxnRef = idTechnician
                + "_"
                + System.currentTimeMillis();

        String vnp_IpAddr = "127.0.0.1";

        String vnp_TmnCode = VnPayConfig.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();

        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");

        if (bankCode != null && !bankCode.isBlank()) {
            vnp_Params.put("vnp_BankCode", bankCode);
        }

        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);

        vnp_Params.put(
                "vnp_OrderInfo",
                "Thanh toan cong no ky thuat vien: " + idTechnician
        );

        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");

        // ========================================
        // NHẬN DIỆN MOBILE / WEB
        // ========================================

        String userAgent = paymentDebtRequest.getUserAgent();

        boolean isMobile = userAgent != null
                && userAgent.toLowerCase().contains("mobile");

        String returnUrl;

        if (isMobile) {

            returnUrl =
                    "http://10.0.2.2:8082/api/payment-info/debt/app/";

        } else {

            returnUrl =
                    "http://localhost:8082/api/payment-info/debt/web/";
        }

        vnp_Params.put("vnp_ReturnUrl", returnUrl);

        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        // ========================================
        // THỜI GIAN
        // ========================================

        Calendar cld = Calendar.getInstance(
                TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
        );

        SimpleDateFormat formatter =
                new SimpleDateFormat("yyyyMMddHHmmss");

        String vnp_CreateDate = formatter.format(cld.getTime());

        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);

        String vnp_ExpireDate = formatter.format(cld.getTime());

        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // ========================================
        // SẮP XẾP VÀ MÃ HÓA THAM SỐ
        // ========================================

        List<String> fieldNames = new ArrayList<>(
                vnp_Params.keySet()
        );

        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (int i = 0; i < fieldNames.size(); i++) {

            String fieldName = fieldNames.get(i);

            String fieldValue = vnp_Params.get(fieldName);

            if (fieldValue != null && !fieldValue.isEmpty()) {

                String encodedField = URLEncoder.encode(
                        fieldName,
                        StandardCharsets.UTF_8.toString()
                );

                String encodedValue = URLEncoder.encode(
                        fieldValue,
                        StandardCharsets.UTF_8.toString()
                );

                hashData.append(encodedField)
                        .append("=")
                        .append(encodedValue);

                query.append(encodedField)
                        .append("=")
                        .append(encodedValue);

                if (i < fieldNames.size() - 1) {
                    hashData.append("&");
                    query.append("&");
                }
            }
        }

        // ========================================
        // TẠO CHỮ KÝ
        // ========================================

        String vnp_SecureHash = VnPayConfig.hmacSHA512(
                VnPayConfig.secretKey,
                hashData.toString()
        );

        query.append("&vnp_SecureHash=")
                .append(vnp_SecureHash);

        return VnPayConfig.vnp_PayUrl + "?" + query;
    }

    private boolean processDebtPayment(
            Map<String, String> params
    ) {

        try {

            // 2. Kiểm tra mã Merchant
            if (!VnPayConfig.vnp_TmnCode.equals(
                    params.get("vnp_TmnCode"))) {
                return false;
            }

            // 3. Kiểm tra kết quả giao dịch
            String responseCode = params.get("vnp_ResponseCode");

            String transactionStatus =
                    params.get("vnp_TransactionStatus");

            if (!"00".equals(responseCode)
                    || !"00".equals(transactionStatus)) {
                return false;
            }

            // 4. Lấy mã giao dịch
            String txnRef = params.get("vnp_TxnRef");

            if (txnRef == null || !txnRef.contains("_")) {
                return false;
            }

            String idTechnician = txnRef.substring(
                    0,
                    txnRef.lastIndexOf("_")
            );

            // 5. Lấy số tiền
            long amount = Long.parseLong(
                    params.get("vnp_Amount")
            ) / 100;

            // 6. Cập nhật công nợ
            Object result = technicianService.updateDebtForTechnician(
                    idTechnician,
                    amount
            );

            return !(result instanceof ErrorDTO);

        } catch (Exception e) {

            e.printStackTrace();

            return false;
        }
    }

    @GetMapping("/api/payment-info/debt/app/")
    public ResponseEntity<String> paymentInfoDebtApp(
            @RequestParam Map<String, String> params
    ) {

        boolean success = processDebtPayment(params);

        String deepLink;

        if (success) {
            deepLink = "kingtech://payment-debt/success";
        } else {
            deepLink = "kingtech://payment-debt/failed";
        }

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(deepLink))
                .build();
    }

    @GetMapping("/api/payment-info/debt/web/")
    public void paymentInfoDebtWeb(
            @RequestParam Map<String, String> params,
            HttpServletResponse response
    ) throws IOException {

        boolean success = processDebtPayment(params);

        if (success) {

            response.sendRedirect(
                    "http://localhost:8080/technician/home?payment=success"
            );

            return;
        }

        response.sendRedirect(
                "http://localhost:8080/technician/home?payment=failed"
        );
    }

    @GetMapping(value = "/api/technician/statistic/request/idTechnician={id}")
    public ResponseEntity<Object> statisticRequestCurrentMonth(@PathVariable String id) {
        Object result = technicianService.StatisticRequestCurrentMonth(id);
        if (result instanceof MessageResponse){
            return new ResponseEntity<>(result, ((MessageResponse) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(value = "/api/technician/statistic/revenue/idTechnician={id}")
    public ResponseEntity<Object> statisticRevenueCurrentMonth(@PathVariable String id) {
        Object result = technicianService.StatisticRevenueCurrentMonth(id);
        if (result instanceof MessageResponse){
            return new ResponseEntity<>(result, ((MessageResponse) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value = "/api/technician/statistic/revenue/")
    public ResponseEntity<Object> statisticRevenue(@RequestBody StatisticTechnicianRequest statisticTechnicianRequest) {
        Object result = technicianService.StatisticRevenue(statisticTechnicianRequest);
        if (result instanceof MessageResponse){
            return new ResponseEntity<>(result, ((MessageResponse) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value = "/api/technician/statistic/request/")
    public ResponseEntity<Object> statisticRequest(@RequestBody StatisticTechnicianRequest statisticTechnicianRequest) {
        Object result = technicianService.StatisticRequest(statisticTechnicianRequest);
        if (result instanceof MessageResponse){
            return new ResponseEntity<>(result, ((MessageResponse) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping(value = "/api/admin/level")
    public ResponseEntity<Object> updateLevelTechnician(){
        MessageResponse messageResponse = technicianService.updateLevel();
        return new ResponseEntity<>(messageResponse, HttpStatus.OK);
    }

    @PostMapping("/api/technician/lat-long")
    public ResponseEntity<?> updateLocation(
            @RequestBody TechnicianLocationRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        technicianService.updateLocationTechnician(request, userDetails);
        return ResponseEntity.ok("Cập nhật vị trí thành công");
    }
}
