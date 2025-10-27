package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.*;
import com.example.bookingapp.Models.DTO.DetailInvoiceDTO;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.InvoicesDTO;
import com.example.bookingapp.Models.DTO.MessageDTO;
import com.example.bookingapp.Models.Request.InvoiceRequest;
import com.example.bookingapp.Repository.CustomerRepository;
import com.example.bookingapp.Repository.InvoicesRepository;
import com.example.bookingapp.Repository.PaymentMethodRepository;
import com.example.bookingapp.Repository.RepairRequestRepository;
import com.example.bookingapp.Services.InvoicesService;
import com.example.bookingapp.Utils.RandomIdUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
public class InvoicesServiceImpl implements InvoicesService {
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    RepairRequestRepository repairRequestRepository;
    @Autowired
    PaymentMethodRepository paymentMethodRepository;
    @Autowired
    InvoicesRepository invoicesRepository;
    @Autowired
    ModelMapper modelMapper;
    @Override
    public Object createInvoice(InvoiceRequest invoiceRequest) {
        MessageDTO messageDTO = new MessageDTO();
        ErrorDTO errorDTO = new ErrorDTO();
        CustomerEntity customerEntity = null;
        PaymentMethodEntity paymentMethodEntity = null;
        float total_amont = 0.0F;
        try {
            //Tìm kiếm request
            RepairRequestEntity repairRequestEntity = repairRequestRepository.findById(invoiceRequest.getRequest_id()).get();
            //Tìm kiếm khách hàng
            try {
                customerEntity = customerRepository.findById(invoiceRequest.getCustomer_id()).get();
            }catch (NoSuchElementException ex){
                errorDTO.setMessage("Can not found customer");
                errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
                return errorDTO;
            }
            //Tìm kiếm phuương thức thanh toán
            try {
                paymentMethodEntity = paymentMethodRepository.findById(invoiceRequest.getPayment_method_id()).get();
            }catch (NoSuchElementException ex){
                errorDTO.setMessage("Can not found payment method");
                errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
                return errorDTO;
            }
            //Tạo một hóa đơn mới
            InvoicesEntity invoicesEntity = new InvoicesEntity();
            //Random id cho hóa đơn
            invoicesEntity.setId_invoices(RandomIdUtils.generateRandomId("In", 10));
            invoicesEntity.setCreated_at(LocalDateTime.now());
            invoicesEntity.setUpdated_at(LocalDateTime.now());
            invoicesEntity.setCustomerEntity(customerEntity);
            invoicesEntity.setPaymentMethodEntity(paymentMethodEntity);
            invoicesEntity.setRepairRequestEntity(repairRequestEntity);
            invoicesEntity.setPaid_at(LocalDate.now());
            for (DetailInvoiceDTO detailInvoiceDTO : invoiceRequest.getDetailInvoiceDTOS()){
                DetailInvoicesEntity detailInvoicesEntity = new DetailInvoicesEntity();
                //tính tổng giá của toàn bộ hóa đơn
                total_amont += detailInvoiceDTO.getQuantity() * detailInvoiceDTO.getPrice();
                modelMapper.map(detailInvoiceDTO, detailInvoicesEntity);
                detailInvoicesEntity.setInvoicesEntity(invoicesEntity);
                detailInvoicesEntity.setInvoicesEntity(invoicesEntity);
                //thêm vào trong list detail invoice
                invoicesEntity.getDetailInvoicesEntities().add(detailInvoicesEntity);
            }
            invoicesEntity.setTotal_amount(total_amont);
            //Lưu dữ liệu
            invoicesRepository.save(invoicesEntity);
            messageDTO.setMessage("Success");
            messageDTO.setHttpStatus(HttpStatus.OK);
            return messageDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found request");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }
}
