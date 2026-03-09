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
import org.springframework.format.annotation.DateTimeFormat;
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
    public Page<FeedbackDTO> getAll(Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        Page<FeedbackEntity> feedbackEntities = feedbackRepository.findAll(pageable);
        List<FeedbackDTO> feedbackDTOS = new ArrayList<>();
        for (FeedbackEntity feedbackEntity : feedbackEntities){
            FeedbackDTO feedbackDTO = new FeedbackDTO();
            feedbackDTO.setId_feedback(feedbackEntity.getId_feedback());
            feedbackDTO.setContent(feedbackEntity.getContent());
            if(feedbackEntity.getRepairRequestEntity() != null){
                feedbackDTO.setId_request(feedbackEntity.getRepairRequestEntity().getId_request());
                feedbackDTO.setDescription(feedbackEntity.getRepairRequestEntity().getDescription());
                if(feedbackEntity.getRepairRequestEntity().getTechnicianEntity() != null){
                    feedbackDTO.setId_technician(feedbackEntity.getRepairRequestEntity().getTechnicianEntity().getId_user());
                    feedbackDTO.setName_techinician(feedbackEntity.getRepairRequestEntity().getTechnicianEntity().getFull_name());
                }
                feedbackDTO.setName_service(feedbackEntity.getRepairRequestEntity().getServiceEntity().getName_service());
            }
            feedbackDTO.setName_customer(feedbackEntity.getCustomerEntity().getFull_name());
            feedbackDTO.setPhone_number_customer(feedbackEntity.getCustomerEntity().getPhone_number());
            feedbackDTO.setEmail_customer(feedbackEntity.getCustomerEntity().getEmail());
            feedbackDTO.setCreated_at(feedbackEntity.getCreated_at());
            feedbackDTO.setUpdated_at(feedbackEntity.getUpdated_at());
            feedbackDTOS.add(feedbackDTO);
        }

        return new PageImpl<>(feedbackDTOS, feedbackEntities.getPageable(), feedbackEntities.getTotalElements());
    }

    @Override
    public Object detailFeedback(Long id) {
        ErrorDTO errorDTO = new ErrorDTO();
        try{
            DetailFeedbackDTO feedbackDTO = new DetailFeedbackDTO();
            FeedbackEntity feedbackEntity = feedbackRepository.findById(id).get();
            RepairRequestEntity repairRequestEntity = feedbackEntity.getRepairRequestEntity();
            modelMapper.map(feedbackEntity, feedbackDTO);
            if(repairRequestEntity != null){
                modelMapper.map(repairRequestEntity, feedbackDTO);
                feedbackDTO.setName_service(repairRequestEntity.getServiceEntity().getName_service());
                feedbackDTO.setId_technician(repairRequestEntity.getTechnicianEntity().getId_user());
                feedbackDTO.setName_techinician(repairRequestEntity.getTechnicianEntity().getFull_name());
            }
            feedbackDTO.setPhone_number_customer(feedbackEntity.getCustomerEntity().getPhone_number());
            feedbackDTO.setName_customer(feedbackEntity.getCustomerEntity().getFull_name());
            feedbackDTO.setEmail_customer(feedbackEntity.getCustomerEntity().getEmail());
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
                "Reply cho feedback " + feedbackEntity.getContent() + " \n" +
                "Xin chào %s,\n\n" +
                        "Tôi là nhân viên chăm sóc khách hàng của KingTech\n" +
                         replyFeedbackRequest.getBody() + "\n\n" +
                        "Trân trọng!\n" +
                        "From KingTech with love",
                customerEntity.getFull_name()
        );
        mailService.sendEmail(customerEntity.getEmail(), "Reply feedback - KingTech", emailContent);
        feedbackRepository.delete(feedbackEntity);
        MessageResponse response = new MessageResponse();
        response.setMessage("Reply success");
        response.setHttpStatus(HttpStatus.OK);
        return response;
    }
}
