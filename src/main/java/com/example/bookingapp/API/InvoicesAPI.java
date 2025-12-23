package com.example.bookingapp.API;

import com.example.bookingapp.Config.VnPayConfig;
import com.example.bookingapp.Entity.InvoicesEntity;
import com.example.bookingapp.Models.DTO.DataDTO;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.InvoicesDTO;
import com.example.bookingapp.Models.Request.InvoiceRequest;
import com.example.bookingapp.Models.Request.PaymentRequest;
import com.example.bookingapp.Models.Response.MessageResponse;
import com.example.bookingapp.Services.InvoicesService;
import com.example.bookingapp.Services.TechnicianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class InvoicesAPI {
    @Autowired
    InvoicesService invoicesService;
    @Autowired
    TechnicianService technicianService;
    @PostMapping(value = "/api/invoices/")
    public ResponseEntity<Object> createInvoices(@RequestBody InvoiceRequest invoiceRequest){
        Object result = invoicesService.createInvoice(invoiceRequest);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO)result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PutMapping(value = "/api/invoices/id={id_invoice}")
    public ResponseEntity<Object> updateStatusInvoices(@PathVariable String id_invoice){
        Object result = invoicesService.updateStatusInvoice(id_invoice);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO)result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(value = "/api/customer/invoices/id={id_customer}")
    public ResponseEntity<Object> getInvoicesByCustomer(@PathVariable String id_customer, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo){
        Page<InvoicesDTO> invoicesDTOS = invoicesService.getInvoiceByCustomer(id_customer, pageNo);
        if (invoicesDTOS == null){
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setMessage("Can not found customer");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setTotal_page(invoicesDTOS.getTotalPages());
        dataDTO.setData(invoicesDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/api/customer/invoices/id-invoice={id_invoice}")
    public ResponseEntity<Object> getDetailInvoice(@PathVariable String id_invoice){
        Object result = invoicesService.getDetailInvoices(id_invoice);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO)result).getHttpStatus());
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setData(result);
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @PostMapping(value = "/api/customer/payment/")
    public String PaymentInvoice(@RequestBody PaymentRequest paymentRequest) throws UnsupportedEncodingException {

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";

        //thay chổ này
        String requestType = paymentRequest.getRequestType();
        Long amount = paymentRequest.getAmount().longValue() * 100;
        String bankCode = paymentRequest.getBank();
        //chổ này là mã đơn hàng
        String vnp_TxnRef = paymentRequest.getId_request();

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
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", requestType);

        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", VnPayConfig.vnp_ReturnUrl);
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

    @GetMapping(value = "/api/payment-info/")
    public ResponseEntity<Object> paymentInfo(
            @RequestParam(value = "vnp_ResponseCode") String vnp_ResponseCode,
            @RequestParam(value = "vnp_TxnRef") String vnp_TxnRef){

        if (vnp_ResponseCode.equals("00")){
            Object result = invoicesService.updateStatusInvoice(vnp_TxnRef);
            if (result instanceof ErrorDTO){
                return new ResponseEntity<>(result, ((ErrorDTO)result).getHttpStatus());
            }
            //công thêm số tiền vào công nợ của thợ và gửi thông báo cho thợ là thanh toán thành công
            technicianService.updateTechnicianBalance(vnp_TxnRef);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }else {
            MessageResponse messageResponse = new MessageResponse();
            messageResponse.setMessage("Payment failed");
            messageResponse.setHttpStatus(HttpStatus.OK);
            return new ResponseEntity<>(messageResponse, HttpStatus.OK);
        }
    }
}
