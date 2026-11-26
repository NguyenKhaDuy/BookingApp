package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.CustomerVouchersDTO;
import com.example.bookingapp.Models.DTO.VoucherDTO;
import com.example.bookingapp.Models.Request.SendVoucherRequest;
import com.example.bookingapp.Models.Request.VoucherRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface VouchersService {
    Page<VoucherDTO> getAllVouchers(Integer pageNo);
    Object getDetailVoucher(String voucherId);
    Object addVoucher(VoucherRequest voucherRequest);
    Object updateVoucher(VoucherRequest voucherRequest);
    Object deleteVoucher(String voucherId);
    boolean verifyVoucher(String customerVoucherId);
    List<CustomerVouchersDTO> getVoucherByCustomer(String idCustomer);
    Object sendVoucher(SendVoucherRequest sendVoucherRequest);
}
