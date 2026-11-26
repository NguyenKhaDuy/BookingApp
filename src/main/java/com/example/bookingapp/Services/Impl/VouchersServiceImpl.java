package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.*;
import com.example.bookingapp.Models.DTO.CustomerVouchersDTO;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.ServiceVoucher;
import com.example.bookingapp.Models.DTO.VoucherDTO;
import com.example.bookingapp.Models.Request.SendVoucherRequest;
import com.example.bookingapp.Models.Request.VoucherRequest;
import com.example.bookingapp.Models.Response.MessageResponse;
import com.example.bookingapp.Repository.*;
import com.example.bookingapp.Services.VouchersService;
import com.example.bookingapp.Utils.RandomIdUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class VouchersServiceImpl implements VouchersService {
    @Autowired
    VouchersRepository vouchersRepository;
    @Autowired
    CustomerVouchersRepository customerVouchersRepository;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    ServiceRepository serviceRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    PromotionsRepository promotionsRepository;
    @Autowired
    RepairRequestRepository repairRequestRepository;
    @Autowired
    private StatusRepository statusRepository;

    @Override
    public Page<VoucherDTO> getAllVouchers(Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 20);
        Page<VouchersEntity> vouchersEntities = vouchersRepository.findAll(pageable);
        List<VoucherDTO> voucherDTOS = vouchersEntities.stream().map(vouchersEntity -> {
            VoucherDTO voucherDTO = new VoucherDTO();
            modelMapper.map(vouchersEntity, voucherDTO);
            voucherDTO.setIdPromotion(vouchersEntity.getPromotionsEntity().getIdPromotion());
            voucherDTO.setName(vouchersEntity.getPromotionsEntity().getName());
            voucherDTO.setStartDate(vouchersEntity.getPromotionsEntity().getStartDate());
            voucherDTO.setEndDate(vouchersEntity.getPromotionsEntity().getEndDate());
            voucherDTO.setPromotionStatus(vouchersEntity.getPromotionsEntity().getPromotionStatus());
            voucherDTO.setDiscountValue(vouchersEntity.getPromotionsEntity().getDiscountValue());
            voucherDTO.setMaxDiscount(vouchersEntity.getPromotionsEntity().getMaxDiscount());
            voucherDTO.setMinOrderValue(vouchersEntity.getPromotionsEntity().getMinOrderValue());

            //lấy tên các dịch vụ
            List<ServiceVoucher> serviceVouchers = vouchersEntity.getPromotionsEntity().getPromotionServiceEntities().stream().map(promotionServiceEntity -> {
                ServiceVoucher serviceVoucher = new ServiceVoucher();
                serviceVoucher.setIdService(promotionServiceEntity.getServiceEntity().getId_service());
                serviceVoucher.setServiceName(promotionServiceEntity.getServiceEntity().getNameService());
                return serviceVoucher;
            }).toList();
            voucherDTO.setServiceVoucher(serviceVouchers);

            return voucherDTO;
        }).toList();
        return new PageImpl<>(voucherDTOS, vouchersEntities.getPageable(), vouchersEntities.getTotalElements());
    }

    @Override
    public Object getDetailVoucher(String voucherId) {
        ErrorDTO errorDTO = new ErrorDTO();
        VouchersEntity vouchersEntity = null;
        VoucherDTO voucherDTO = null;
        try{
            vouchersEntity = vouchersRepository.findById(voucherId).get();
            voucherDTO = new VoucherDTO();
            modelMapper.map(vouchersEntity, voucherDTO);
            voucherDTO.setIdPromotion(vouchersEntity.getPromotionsEntity().getIdPromotion());
            voucherDTO.setName(vouchersEntity.getPromotionsEntity().getName());
            voucherDTO.setStartDate(vouchersEntity.getPromotionsEntity().getStartDate());
            voucherDTO.setEndDate(vouchersEntity.getPromotionsEntity().getEndDate());
            voucherDTO.setPromotionStatus(vouchersEntity.getPromotionsEntity().getPromotionStatus());
            voucherDTO.setDiscountValue(vouchersEntity.getPromotionsEntity().getDiscountValue());
            voucherDTO.setMaxDiscount(vouchersEntity.getPromotionsEntity().getMaxDiscount());
            voucherDTO.setMinOrderValue(vouchersEntity.getPromotionsEntity().getMinOrderValue());

            //lấy tên các dịch vụ
            List<ServiceVoucher> serviceVouchers = vouchersEntity.getPromotionsEntity().getPromotionServiceEntities().stream().map(promotionServiceEntity -> {
                ServiceVoucher serviceVoucher = new ServiceVoucher();
                serviceVoucher.setIdService(promotionServiceEntity.getServiceEntity().getId_service());
                serviceVoucher.setServiceName(promotionServiceEntity.getServiceEntity().getNameService());
                return serviceVoucher;
            }).toList();
            voucherDTO.setServiceVoucher(serviceVouchers);
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found voucher");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
        return voucherDTO;
    }

    @Override
    public Object addVoucher(VoucherRequest voucherRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageResponse messageResponse = new MessageResponse();
        PromotionsEntity promotionsEntity = null;
        try{
            promotionsEntity = promotionsRepository.findById(voucherRequest.getPromotionId()).get();
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found promotion");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
        VouchersEntity vouchersEntity = new VouchersEntity();
        modelMapper.map(voucherRequest, vouchersEntity);
        vouchersEntity.setIdVoucher(RandomIdUtils.generateRandomId());
        vouchersEntity.setStatus(voucherRequest.getStatus());
        vouchersEntity.setCreated_at(LocalDateTime.now());
        vouchersEntity.setUpdated_at(LocalDateTime.now());
        vouchersEntity.setPromotionsEntity(promotionsEntity);
        vouchersRepository.save(vouchersEntity);
        messageResponse.setMessage("Success");
        messageResponse.setHttpStatus(HttpStatus.OK);
        return messageResponse;
    }

    @Override
    public Object updateVoucher(VoucherRequest voucherRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageResponse messageResponse = new MessageResponse();
        VouchersEntity vouchersEntity = null;
        PromotionsEntity promotionsEntity = null;
        try{
            promotionsEntity = promotionsRepository.findById(voucherRequest.getPromotionId()).get();
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found promotion");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
        try{
            vouchersEntity = vouchersRepository.findById(voucherRequest.getIdVoucher()).get();
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found voucher");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
        modelMapper.map(voucherRequest, vouchersEntity);
        vouchersEntity.setIdVoucher(RandomIdUtils.generateRandomId());
        vouchersEntity.setStatus(voucherRequest.getStatus());
        vouchersEntity.setUpdated_at(LocalDateTime.now());
        vouchersEntity.setPromotionsEntity(promotionsEntity);
        vouchersRepository.save(vouchersEntity);
        messageResponse.setMessage("Success");
        messageResponse.setHttpStatus(HttpStatus.OK);
        return messageResponse;
    }

    @Override
    public Object deleteVoucher(String voucherId) {
        ErrorDTO errorDTO = new ErrorDTO();
        VouchersEntity vouchersEntity = null;
        MessageResponse messageResponse = new MessageResponse();
        try{
            vouchersEntity = vouchersRepository.findById(voucherId).get();
            vouchersRepository.delete(vouchersEntity);
            messageResponse.setMessage("Success");
            messageResponse.setHttpStatus(HttpStatus.OK);
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found voucher");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
        return messageResponse;
    }

    @Override
    public boolean verifyVoucher(String customerVoucherId) {
        CustomerVouchersEntity customerVouchersEntity = customerVouchersRepository.findById(customerVoucherId).get();
        if (checkStatus(customerVouchersEntity.getStatus())
                && checkLimit(customerVouchersEntity.getUsageLimit(), customerVouchersEntity.getUsageCount())
                && checkExpires(customerVouchersEntity.getExpiresAt())
                && checkMaxUsageVoucher(customerVouchersEntity.getVouchersEntity())
        ){
            return true;
        }
        return false;
    }

    public boolean checkStatus(VoucherStatus voucherStatus) {
        boolean result = false;
        if (voucherStatus.equals("AVAILABLE")){
            result = true;
        }
        return result;
    }

    // Kiểm tra voucher còn lượt sử dụng hay không
    public boolean checkLimit(Long usageLimit, Long usageCount) {
        if (usageLimit == null || usageCount == null) {
            return false;
        }

        return usageLimit > 0
                && usageCount >= 0
                && usageCount < usageLimit;
    }

    // Kiểm tra voucher còn thời hạn hay không
    public boolean checkExpires(LocalDateTime expires) {
        return expires != null
                && expires.isAfter(LocalDateTime.now());
    }

    public boolean checkMaxUsageVoucher(VouchersEntity vouchersEntity) {
        return vouchersEntity != null && !(vouchersEntity.getUsedCount() >= vouchersEntity.getMaxUsage());
    }

    @Override
    public List<CustomerVouchersDTO> getVoucherByCustomer(String idCustomer) {
        CustomerEntity customerEntity = null;
        try {
            customerEntity = customerRepository.findById(idCustomer).get();

        }catch (NoSuchElementException ex){
            return null;
        }
            List<CustomerVouchersEntity> customerVouchersEntities = customerVouchersRepository.findByCustomerEntity(customerEntity);
            List<CustomerVouchersDTO> customerVouchersDTOS = customerVouchersEntities.stream().map(customerVouchersEntity -> {
                CustomerVouchersDTO customerVouchersDTO = new CustomerVouchersDTO();
                modelMapper.map(customerVouchersEntity, customerVouchersDTO);
                customerVouchersDTO.setIdVoucher(customerVouchersEntity.getVouchersEntity().getIdVoucher());
                customerVouchersDTO.setCode(customerVouchersEntity.getVouchersEntity().getCode());
                customerVouchersDTO.setDiscountValue(customerVouchersEntity.getVouchersEntity().getPromotionsEntity().getDiscountValue());
                customerVouchersDTO.setMaxDiscount(customerVouchersEntity.getVouchersEntity().getPromotionsEntity().getMaxDiscount());
                customerVouchersDTO.setMinOrderValue(customerVouchersEntity.getVouchersEntity().getPromotionsEntity().getMinOrderValue());
                customerVouchersDTO.setIdPromotion(customerVouchersEntity.getVouchersEntity().getPromotionsEntity().getIdPromotion());
                customerVouchersDTO.setName(customerVouchersEntity.getVouchersEntity().getPromotionsEntity().getName());
                customerVouchersDTO.setMaxUsage(customerVouchersEntity.getVouchersEntity().getMaxUsage());
                customerVouchersDTO.setUsedCount(customerVouchersEntity.getVouchersEntity().getUsedCount());
                //lấy tên các dịch vụ
                List<ServiceVoucher> serviceVouchers = customerVouchersEntity.getVouchersEntity().getPromotionsEntity().getPromotionServiceEntities().stream().map(promotionServiceEntity -> {
                    ServiceVoucher serviceVoucher = new ServiceVoucher();
                    serviceVoucher.setIdService(promotionServiceEntity.getServiceEntity().getId_service());
                    serviceVoucher.setServiceName(promotionServiceEntity.getServiceEntity().getNameService());
                    return serviceVoucher;
                }).toList();
                customerVouchersDTO.setServiceVouchers(serviceVouchers);

                return customerVouchersDTO;
            }).toList();

        return customerVouchersDTOS;
    }

    @Override
    public Object sendVoucher(SendVoucherRequest sendVoucherRequest) {
        MessageResponse messageResponse = new MessageResponse();
        ErrorDTO errorDTO = new ErrorDTO();
        List<CustomerEntity> customerEntities = null;
        if (sendVoucherRequest.getSendVoucherType().equals(TypeSendVoucher.ALL_CUSTOMERS)){
            customerEntities = customerRepository.findAll();
        }
        if (sendVoucherRequest.getSendVoucherType().equals(TypeSendVoucher.LOYAL_CUSTOMERS)){
            customerEntities = fillterLoyalCustomers();
        }
        if (sendVoucherRequest.getSendVoucherType().equals(TypeSendVoucher.SERVICE_CUSTOMERS)){
            customerEntities = fillterServiceCustomers(sendVoucherRequest);
        }

        VouchersEntity vouchersEntity = null;
        try {
            vouchersEntity = vouchersRepository.findById(sendVoucherRequest.getVoucherId()).get();
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found voucher");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
        for (CustomerEntity customerEntity : customerEntities) {
            CustomerVouchersEntity customerVouchersEntity = new CustomerVouchersEntity();
            customerVouchersEntity.setIdCustomerVoucher(RandomIdUtils.generateRandomId());
            customerVouchersEntity.setUsageCount(0L);
            customerVouchersEntity.setUsageLimit(sendVoucherRequest.getUsageLimit());
            customerVouchersEntity.setReceivedAt(LocalDateTime.now());
            customerVouchersEntity.setStatus(VoucherStatus.AVAILABLE);
            customerVouchersEntity.setExpiresAt(sendVoucherRequest.getExpiresAt());
            customerVouchersEntity.setCustomerEntity(customerEntity);
            customerVouchersEntity.setVouchersEntity(vouchersEntity);
            customerVouchersRepository.save(customerVouchersEntity);
        }
        messageResponse.setMessage("Successfully sent voucher");
        messageResponse.setHttpStatus(HttpStatus.OK);
        return messageResponse;
    }



    public List<CustomerEntity> fillterLoyalCustomers(){
        List<CustomerEntity> customerEntities = customerRepository.findAll()
                .stream()
                .filter(customer -> customer.getRepairRequestEntities()
                        .stream()
                        .anyMatch(request -> "COMPLETED".equals(
                                request.getStatusEntity().getNameStatus()
                        )))
                .sorted(Comparator.comparingLong(
                        (CustomerEntity customer) -> customer.getRepairRequestEntities()
                                .stream()
                                .filter(request -> "COMPLETED".equals(
                                        request.getStatusEntity().getNameStatus()
                                ))
                                .count()
                ).reversed())
                .limit(20)
                .toList();
        return customerEntities;
    }


    public List<CustomerEntity> fillterServiceCustomers(SendVoucherRequest sendVoucherRequest){
        List<CustomerEntity> customerEntities = new ArrayList<CustomerEntity>();
        ServiceEntity serviceEntity = null;
        StatusEntity statusEntity = null;
        try {
            serviceEntity = serviceRepository.findById(sendVoucherRequest.getServiceId()).get();
        }catch (NoSuchElementException ex){
            return null;
        }
        try {
            statusEntity = statusRepository.findByNameStatus("COMPLETED");
        }catch (NoSuchElementException ex){
            return null;
        }
        List<RepairRequestEntity> requestEntities = repairRequestRepository.findByServiceAndStatusInMonth(serviceEntity.getId_service(), "COMPLETED", sendVoucherRequest.getMonth());
        for (RepairRequestEntity repairRequestEntity : requestEntities) {
            if (repairRequestEntity.getInvoicesEntity().getTotal_amount() > sendVoucherRequest.getMinValueInvoice()){
                customerEntities.add(repairRequestEntity.getCustomerEntity());
            }
        }
        return customerEntities;
    }

}
