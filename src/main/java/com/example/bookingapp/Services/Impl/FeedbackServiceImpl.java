package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.CustomerEntity;
import com.example.bookingapp.Entity.FeedbackEntity;
import com.example.bookingapp.Entity.RepairRequestEntity;
import com.example.bookingapp.Models.DTO.*;
import com.example.bookingapp.Models.Request.FeedbackRequest;
import com.example.bookingapp.Repository.CustomerRepository;
import com.example.bookingapp.Repository.FeedbackRepository;
import com.example.bookingapp.Repository.RepairRequestRepository;
import com.example.bookingapp.Services.FeedbackService;
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
}
