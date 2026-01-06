package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.*;
import com.example.bookingapp.Models.DTO.*;
import com.example.bookingapp.Models.Request.FeedbackRequest;
import com.example.bookingapp.Models.Request.ReplyFeedbackRequest;
import com.example.bookingapp.Models.Response.MessageResponse;
import com.example.bookingapp.Repository.CustomerRepository;
import com.example.bookingapp.Repository.FeedbackRepository;
import com.example.bookingapp.Repository.RepairRequestRepository;
import com.example.bookingapp.Services.FeedbackService;
import com.example.bookingapp.Services.MailService;
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
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class FeedbackServiceImpl implements FeedbackService {
    @Autowired
    RepairRequestRepository repairRequestRepository;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    FeedbackRepository feedbackRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    MailService mailService;
    @Override
    public Object createFeedback(FeedbackRequest feedbackRequest) {
        MessageResponse messageResponse = new MessageResponse();
        ErrorDTO errorDTO = new ErrorDTO();
        RepairRequestEntity repairRequestEntity = null;
        try {
            if(feedbackRequest.getRequest_id() != null){
                repairRequestEntity = repairRequestRepository.findById(feedbackRequest.getRequest_id()).get();
            }
            try {
                CustomerEntity customerEntity = customerRepository.findById(feedbackRequest.getCustomer_id()).get();
                FeedbackEntity feedbackEntity = new FeedbackEntity();
                feedbackEntity.setContent(feedbackRequest.getContent());
                feedbackEntity.setRepairRequestEntity(repairRequestEntity);
                feedbackEntity.setCustomerEntity(customerEntity);
                feedbackEntity.setCreated_at(LocalDateTime.now());
                feedbackEntity.setUpdated_at(LocalDateTime.now());
                feedbackRepository.save(feedbackEntity);
                messageResponse.setMessage("Success");
                messageResponse.setHttpStatus(HttpStatus.OK);
                return messageResponse;
            }catch (NoSuchElementException ex){
                errorDTO.setMessage("Can not found customer");
                errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
                return errorDTO;
            }
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found request");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Page<RequestFeedbackDTO> getAll(Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        Page<RepairRequestEntity> repairRequestEntities = repairRequestRepository.findAll(pageable);
        List<RequestFeedbackDTO> requestFeedbackDTOS = new ArrayList<>();
        for (RepairRequestEntity repairRequestEntity : repairRequestEntities){
            RequestFeedbackDTO requestFeedbackDTO = new RequestFeedbackDTO();
            modelMapper.map(repairRequestEntity, requestFeedbackDTO);
            requestFeedbackDTO.setName_service(repairRequestEntity.getServiceEntity().getName_service());
            requestFeedbackDTO.setId_technician(repairRequestEntity.getTechnicianEntity().getId_user());
            requestFeedbackDTO.setName_techinician(repairRequestEntity.getTechnicianEntity().getFull_name());
            requestFeedbackDTO.setPhone_number_customer(repairRequestEntity.getCustomerEntity().getPhone_number());
            requestFeedbackDTO.setName_customer(repairRequestEntity.getCustomerEntity().getFull_name());
            requestFeedbackDTO.setEmail_customer(repairRequestEntity.getCustomerEntity().getEmail());

            for (FeedbackEntity feedbackEntity : repairRequestEntity.getFeedbackEntities()){
                FeedbackDTO feedbackDTO = new FeedbackDTO();
                modelMapper.map(feedbackEntity, feedbackDTO);
                requestFeedbackDTO.getFeedbackDTOS().add(feedbackDTO);
            }

            requestFeedbackDTOS.add(requestFeedbackDTO);
        }
        return new PageImpl<>(requestFeedbackDTOS, repairRequestEntities.getPageable(), repairRequestEntities.getTotalElements());
    }

    @Override
    public Object detailFeedback(Long id) {
        ErrorDTO errorDTO = new ErrorDTO();
        try{
            DetailFeedbackDTO feedbackDTO = new DetailFeedbackDTO();
            FeedbackEntity feedbackEntity = feedbackRepository.findById(id).get();
            RepairRequestEntity repairRequestEntity = feedbackEntity.getRepairRequestEntity();
            modelMapper.map(feedbackEntity, feedbackDTO);
            modelMapper.map(repairRequestEntity, feedbackDTO);
            feedbackDTO.setName_service(repairRequestEntity.getServiceEntity().getName_service());
            feedbackDTO.setId_technician(repairRequestEntity.getTechnicianEntity().getId_user());
            feedbackDTO.setName_techinician(repairRequestEntity.getTechnicianEntity().getFull_name());
            feedbackDTO.setPhone_number_customer(repairRequestEntity.getCustomerEntity().getPhone_number());
            feedbackDTO.setName_customer(repairRequestEntity.getCustomerEntity().getFull_name());
            feedbackDTO.setEmail_customer(repairRequestEntity.getCustomerEntity().getEmail());
            return feedbackDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found feedback");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object replyFeedback(ReplyFeedbackRequest replyFeedbackRequest) {
        FeedbackEntity feedbackEntity = feedbackRepository.findById(replyFeedbackRequest.getId_feedback()).get();
        CustomerEntity customerEntity = feedbackEntity.getCustomerEntity();
        String emailContent = String.format(
                "Xin chào %s,\n\n" +
                        "Tôi là nhân viên chăm sóc khách hàng của Bookingapp\n" +
                         replyFeedbackRequest.getBody() + "\n\n" +
                        "Trân trọng!\n" +
                        "From Booking app with love",
                customerEntity.getFull_name()
        );
        mailService.sendEmail(customerEntity.getEmail(), "Mã xác thực otp - Booking app", emailContent);
        MessageResponse response = new MessageResponse();
        response.setMessage("Reply success");
        response.setHttpStatus(HttpStatus.OK);
        return response;
    }
}
