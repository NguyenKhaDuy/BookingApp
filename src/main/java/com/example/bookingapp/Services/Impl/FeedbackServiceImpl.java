package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.CustomerEntity;
import com.example.bookingapp.Entity.FeedbackEntity;
import com.example.bookingapp.Entity.RepairRequestEntity;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.MessageDTO;
import com.example.bookingapp.Models.Request.FeedbackRequest;
import com.example.bookingapp.Repository.CustomerRepository;
import com.example.bookingapp.Repository.FeedbackRepository;
import com.example.bookingapp.Repository.RepairRequestRepository;
import com.example.bookingapp.Services.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
public class FeedbackServiceImpl implements FeedbackService {
    @Autowired
    RepairRequestRepository repairRequestRepository;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    FeedbackRepository feedbackRepository;
    @Override
    public Object createFeedback(FeedbackRequest feedbackRequest) {
        MessageDTO messageDTO = new MessageDTO();
        ErrorDTO errorDTO = new ErrorDTO();
        try {
            RepairRequestEntity repairRequestEntity = repairRequestRepository.findById(feedbackRequest.getRequest_id()).get();
            try {
                CustomerEntity customerEntity = customerRepository.findById(feedbackRequest.getCustomer_id()).get();
                FeedbackEntity feedbackEntity = new FeedbackEntity();
                feedbackEntity.setContent(feedbackRequest.getContent());
                feedbackEntity.setRepairRequestEntity(repairRequestEntity);
                feedbackEntity.setCustomerEntity(customerEntity);
                feedbackEntity.setCreated_at(LocalDateTime.now());
                feedbackEntity.setUpdated_at(LocalDateTime.now());
                feedbackRepository.save(feedbackEntity);
                messageDTO.setMessage("Success");
                messageDTO.setHttpStatus(HttpStatus.OK);
                return messageDTO;
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
}
