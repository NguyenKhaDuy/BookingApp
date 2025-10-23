package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.CustomerEntity;
import com.example.bookingapp.Entity.RatingEntity;
import com.example.bookingapp.Entity.TechnicianEntity;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.MessageDTO;
import com.example.bookingapp.Models.DTO.RatingDTO;
import com.example.bookingapp.Models.Request.RatingRequest;
import com.example.bookingapp.Repository.CustomerRepository;
import com.example.bookingapp.Repository.RatingRepository;
import com.example.bookingapp.Repository.TechnicianRepository;
import com.example.bookingapp.Services.RatingService;
import com.example.bookingapp.Utils.ConvertByteToBase64;
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
public class RatingServiceImpl implements RatingService {
    @Autowired
    RatingRepository ratingRepository;

    @Autowired
    TechnicianRepository technicianRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public Object getAllRatingsByTechnician(Integer pageNo, String id_technician) {
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        try {
            TechnicianEntity technicianEntity = technicianRepository.findById(id_technician).get();
            Page<RatingEntity> ratingEntities = ratingRepository.findByTechnicianEntity(technicianEntity, pageable);
            List<RatingDTO> ratingDTOS = new ArrayList<>();
            for(RatingEntity ratingEntity : ratingEntities){
                RatingDTO ratingDTO = new RatingDTO();
                modelMapper.map(ratingEntity, ratingDTO);
                ratingDTO.setId_user(ratingEntity.getCustomerEntity().getId_user());
                ratingDTO.setFull_name(ratingEntity.getCustomerEntity().getFull_name());
                ratingDTO.setAvatarBase64(ConvertByteToBase64.toBase64(ratingEntity.getCustomerEntity().getAvatar()));
                ratingDTOS.add(ratingDTO);
            }
            return new PageImpl<>(ratingDTOS, ratingEntities.getPageable(), ratingEntities.getTotalElements());
        }catch (NoSuchElementException ex){
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setMessage(ex.getMessage());
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object createRatingTechnician(RatingRequest ratingRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageDTO messageDTO = new MessageDTO();
        try{
            TechnicianEntity technicianEntity = technicianRepository.findById(ratingRequest.getTechnician_id()).get();
            CustomerEntity customerEntity = customerRepository.findById(ratingRequest.getCustomer_id()).get();
            RatingEntity ratingEntity = new RatingEntity();
            ratingEntity.setComment(ratingRequest.getComment());
            ratingEntity.setStars(ratingRequest.getStars());
            ratingEntity.setTechnicianEntity(technicianEntity);
            ratingEntity.setCustomerEntity(customerEntity);
            ratingEntity.setCreated_at(LocalDateTime.now());
            ratingEntity.setUpdated_at(LocalDateTime.now());
            ratingRepository.save(ratingEntity);
            messageDTO.setMessage("Success");
            messageDTO.setHttpStatus(HttpStatus.OK);
            return messageDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found techinician");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object deleteRating(Long id_rating) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageDTO messageDTO = new MessageDTO();
        try{
            RatingEntity ratingEntity = ratingRepository.findById(id_rating).get();
            ratingRepository.delete(ratingEntity);
            messageDTO.setMessage("Success");
            messageDTO.setHttpStatus(HttpStatus.OK);
            return messageDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found rating");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }
}
