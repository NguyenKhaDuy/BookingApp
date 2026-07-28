package com.example.bookingapp.API;

import com.example.bookingapp.Config.VnPayConfig;
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
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
    public ResponseEntity<Object> searchByName(@RequestParam(value = "id_service") Long id_service,
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
            String password  = RandomIdUtils.generateRandomId("P", 5);
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

    @PostMapping(value = "/api/technician/payment/debt/")
    public String PaymentInvoice(@RequestBody PaymentDebtRequest paymentDebtRequest) throws UnsupportedEncodingException {

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";

        //thay chổ này
        Long amount = paymentDebtRequest.getAmount().longValue() * 100;
        String bankCode = paymentDebtRequest.getBank();
        //chổ này là mã đơn hàng
        String vnp_TxnRef =
                paymentDebtRequest.getId_technician()
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

        vnp_Params.put("vnp_BankCode", bankCode);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toán công nợ của thợ:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", "Công nợ");

        vnp_Params.put("vnp_Locale", "vn");

        String returnUrl =
                "http://localhost:8082/api/payment-info/debt/web/";

        vnp_Params.put("vnp_ReturnUrl", returnUrl);

//        String returnUrl =
//                "http://localhost:8080/technician/home";
//
//        vnp_Params.put("vnp_ReturnUrl", returnUrl);

//        vnp_Params.put("vnp_ReturnUrl", VnPayConfig.vnp_ReturnUrl);

        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VnPayConfig.hmacSHA512(VnPayConfig.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = VnPayConfig.vnp_PayUrl + "?" + queryUrl;

        return paymentUrl;
    }

    @GetMapping("/api/payment-info/debt/web/")
    public void paymentInfo(
            @RequestParam("vnp_ResponseCode") String vnp_ResponseCode,
            @RequestParam("vnp_TxnRef") String vnp_TxnRef,
            @RequestParam("vnp_Amount") Long vnp_Amount,
            HttpServletResponse response
    ) throws IOException {

        if ("00".equals(vnp_ResponseCode)) {

            String idTechnician = vnp_TxnRef.split("_")[0];

            Long actualAmount = vnp_Amount / 100;

            Object result =
                    technicianService.updateDebtForTechnician(
                            idTechnician,
                            actualAmount
                    );

            if (!(result instanceof ErrorDTO)) {

                response.sendRedirect(
                        "http://localhost:8080/technician/home?payment=success"
                );

                return;
            }
        }

        response.sendRedirect(
                "http://localhost:8080/technician/home?payment=failed"
        );
    }

}
