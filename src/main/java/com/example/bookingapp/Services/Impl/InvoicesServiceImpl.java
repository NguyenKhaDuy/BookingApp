package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.*;
import com.example.bookingapp.Models.DTO.DetailInvoiceDTO;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.InvoicesDTO;
import com.example.bookingapp.Models.DTO.MessageDTO;
import com.example.bookingapp.Models.Request.InvoiceRequest;
import com.example.bookingapp.Repository.*;
import com.example.bookingapp.Services.InvoicesService;
import com.example.bookingapp.Utils.RandomIdUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    StatusRepository statusRepository;
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
            StatusEntity statusEntity = statusRepository.findByNameStatus("UNPAID");
            //Tìm kiếm khách hàng
            try {
                customerEntity = customerRepository.findById(invoiceRequest.getCustomer_id()).get();
            } catch (NoSuchElementException ex) {
                errorDTO.setMessage("Can not found customer");
                errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
                return errorDTO;
            }
            //Tìm kiếm phuương thức thanh toán
            try {
                paymentMethodEntity = paymentMethodRepository.findById(invoiceRequest.getPayment_method_id()).get();
            } catch (NoSuchElementException ex) {
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
            invoicesEntity.setStatusEntity(statusEntity);
            for (DetailInvoiceDTO detailInvoiceDTO : invoiceRequest.getDetailInvoiceDTOS()) {
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
        } catch (NoSuchElementException ex) {
            errorDTO.setMessage("Can not found request");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object updateStatusInvoice(String id_invoice) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageDTO messageDTO = new MessageDTO();
        try {
            InvoicesEntity invoicesEntity = invoicesRepository.findById(id_invoice).get();
            PaymentMethodEntity paymentMethodEntity = invoicesEntity.getPaymentMethodEntity();
            if (paymentMethodEntity.getName_method().equals("Cash")){
                StatusEntity statusEntity = statusRepository.findByNameStatus("PAID");
                invoicesEntity.setStatusEntity(statusEntity);
                invoicesRepository.save(invoicesEntity);
                messageDTO.setHttpStatus(HttpStatus.OK);
                messageDTO.setMessage("Success");
            }
            messageDTO.setHttpStatus(HttpStatus.OK);
            messageDTO.setMessage("Can not update status for invoice because payment method is not cash");
            return messageDTO;
        } catch (NoSuchElementException ex) {
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            errorDTO.setMessage("Can not found invoice");
            return errorDTO;
        }
    }

    @Override
    public Page<InvoicesDTO> getInvoiceByCustomer(String customer_id, Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        try {
            //Tìm kiếm khách hàng
            CustomerEntity customerEntity = customerRepository.findById(customer_id).get();
            Page<InvoicesEntity> invoicesEntities = invoicesRepository.findByCustomerEntity(customerEntity, pageable);
            List<InvoicesDTO> invoicesDTOS = new ArrayList<>();
            for (InvoicesEntity invoicesEntity : invoicesEntities) {
                InvoicesDTO invoicesDTO = new InvoicesDTO();
                modelMapper.map(invoicesEntity, invoicesDTO);
                invoicesDTO.setName_status(invoicesEntity.getStatusEntity().getNameStatus());
                for (DetailInvoicesEntity detailInvoicesEntity : invoicesEntity.getDetailInvoicesEntities()) {
                    DetailInvoiceDTO detailInvoiceDTO = new DetailInvoiceDTO();
                    modelMapper.map(detailInvoicesEntity, detailInvoiceDTO);
                    invoicesDTO.getDetailInvoiceDTOS().add(detailInvoiceDTO);
                }
                invoicesDTOS.add(invoicesDTO);
            }
            return new PageImpl<>(invoicesDTOS, invoicesEntities.getPageable(), invoicesEntities.getTotalElements());
        } catch (NoSuchElementException ex) {
            return null;
        }
    }

    @Override
    public Object getDetailInvoices(String id_invoice) {
        ErrorDTO errorDTO = new ErrorDTO();
        try {
            InvoicesDTO invoicesDTO = new InvoicesDTO();
            InvoicesEntity invoicesEntity = invoicesRepository.findById(id_invoice).get();
            modelMapper.map(invoicesEntity, invoicesDTO);
            invoicesDTO.setName_status(invoicesEntity.getStatusEntity().getNameStatus());
            for (DetailInvoicesEntity detailInvoicesEntity : invoicesEntity.getDetailInvoicesEntities()){
                DetailInvoiceDTO detailInvoiceDTO = new DetailInvoiceDTO();
                modelMapper.map(detailInvoicesEntity, detailInvoiceDTO);
                invoicesDTO.getDetailInvoiceDTOS().add(detailInvoiceDTO);
            }
            return invoicesDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found invoice");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }
}