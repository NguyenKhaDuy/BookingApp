package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.PromotionServiceEntity;
import com.example.bookingapp.Entity.PromotionStatus;
import com.example.bookingapp.Entity.PromotionsEntity;
import com.example.bookingapp.Entity.ServiceEntity;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.PromotionDTO;
import com.example.bookingapp.Models.DTO.PromotionServiceDTO;
import com.example.bookingapp.Models.Request.PromotionRequest;
import com.example.bookingapp.Models.Response.MessageResponse;
import com.example.bookingapp.Repository.PromotionServiceRepository;
import com.example.bookingapp.Repository.PromotionsRepository;
import com.example.bookingapp.Repository.ServiceRepository;
import com.example.bookingapp.Services.PromotionsService;
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
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PromotionsServiceImpl implements PromotionsService {
    @Autowired
    PromotionServiceRepository promotionServiceRepository;
    @Autowired
    PromotionsRepository promotionsRepository;
    @Autowired
    ServiceRepository serviceRepository;
    @Autowired
    ModelMapper modelMapper;

    @Override
    public Page<PromotionDTO> getPromotions(Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        Page<PromotionsEntity> promotionsEntities = promotionsRepository.findAll(pageable);
        List<PromotionDTO> promotionDTOS = promotionsEntities.stream().map(promotionsEntity -> {
            PromotionDTO promotionDTO = new PromotionDTO();
            modelMapper.map(promotionsEntity, promotionDTO);
            List<PromotionServiceDTO> promotionServiceDTOS = promotionsEntity.getPromotionServiceEntities().stream().map(promotionServiceEntity -> {
                PromotionServiceDTO promotionServiceDTO = new PromotionServiceDTO();
                promotionServiceDTO.setIdPromotionService(promotionServiceEntity.getIdPromotionService());
                promotionServiceDTO.setId_service(promotionServiceEntity.getServiceEntity().getId_service());
                promotionServiceDTO.setNameService(promotionServiceEntity.getServiceEntity().getNameService());
                promotionServiceDTO.setCreated_at(promotionServiceEntity.getCreated_at());
                return promotionServiceDTO;
            }).toList();
            promotionDTO.setPromotionServiceDTOS(promotionServiceDTOS);
            return promotionDTO;
        }).toList();

        return new PageImpl<>(promotionDTOS, promotionsEntities.getPageable(), promotionsEntities.getTotalElements());
    }

    @Override
    public Object getPromotion(String promotionId) {
        ErrorDTO errorDTO = new ErrorDTO();
        PromotionDTO promotionDTO = new PromotionDTO();
        try {
            PromotionsEntity promotionsEntity = promotionsRepository.findById(promotionId).get();
            modelMapper.map(promotionsEntity, promotionDTO);
            List<PromotionServiceDTO> promotionServiceDTOS = promotionsEntity.getPromotionServiceEntities().stream().map(promotionServiceEntity -> {
                PromotionServiceDTO promotionServiceDTO = new PromotionServiceDTO();
                promotionServiceDTO.setIdPromotionService(promotionServiceEntity.getIdPromotionService());
                promotionServiceDTO.setId_service(promotionServiceEntity.getServiceEntity().getId_service());
                promotionServiceDTO.setNameService(promotionServiceEntity.getServiceEntity().getNameService());
                promotionServiceDTO.setCreated_at(promotionServiceEntity.getCreated_at());
                return promotionServiceDTO;
            }).toList();
            promotionDTO.setPromotionServiceDTOS(promotionServiceDTOS);
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not find promotion with id " + promotionId);
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
        return promotionDTO;
    }

    @Override
    public Object deletePromotion(String promotionId) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageResponse messageResponse = new MessageResponse();
        try {
            PromotionsEntity promotionsEntity = promotionsRepository.findById(promotionId).get();
            promotionsRepository.delete(promotionsEntity);
            messageResponse.setMessage("Promotion with id " + promotionId + " deleted successfully");
            messageResponse.setHttpStatus(HttpStatus.OK);
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not find promotion with id " + promotionId);
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
        return messageResponse;
    }

    @Override
    public Object addPromotion(PromotionRequest promotionRequest) {
        MessageResponse messageResponse = new MessageResponse();
        ErrorDTO errorDTO = new ErrorDTO();
        PromotionsEntity promotionsEntity = new PromotionsEntity();
        modelMapper.map(promotionRequest, promotionsEntity);
        promotionsEntity.setIdPromotion(RandomIdUtils.generateRandomId());

        List<PromotionServiceEntity> promotionServiceEntities = new ArrayList<>();
        if (promotionRequest.getIdServices() != null && !promotionRequest.getIdServices().isEmpty()) {
            for (String idService : promotionRequest.getIdServices()) {
                ServiceEntity serviceEntity = null;
                try{
                    serviceEntity = serviceRepository.findById(idService).get();
                }catch (NoSuchElementException ex){
                    errorDTO.setMessage("Can not find service with id " + idService);
                    errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
                    return errorDTO;
                }

                PromotionServiceEntity promotionServiceEntity = new PromotionServiceEntity();
                promotionServiceEntity.setIdPromotionService(RandomIdUtils.generateRandomId());
                promotionServiceEntity.setServiceEntity(serviceEntity);
                promotionServiceEntity.setCreated_at(LocalDateTime.now());
                promotionServiceEntities.add(promotionServiceEntity);
            }
        }
        promotionsEntity.setPromotionServiceEntities(promotionServiceEntities);
        promotionsEntity.setCreated_at(LocalDateTime.now());
        promotionsEntity.setUpdated_at(LocalDateTime.now());
        promotionsEntity.setPromotionStatus(promotionRequest.getPromotionStatus());
        promotionsRepository.save(promotionsEntity);
        messageResponse.setMessage("Success");
        messageResponse.setHttpStatus(HttpStatus.OK);
        return messageResponse;
    }

    @Override
    public Object updatePromotion(PromotionRequest promotionRequest) {
        MessageResponse messageResponse = new MessageResponse();
        ErrorDTO errorDTO = new ErrorDTO();
        PromotionsEntity promotionsEntity = null;
        try {
            promotionsEntity = promotionsRepository.findById(promotionRequest.getIdPromotion()).get();
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not find promotion with id " + promotionRequest.getIdPromotion());
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
        modelMapper.map(promotionRequest, promotionsEntity);
        promotionsEntity.getPromotionServiceEntities().clear();
        List<PromotionServiceEntity> promotionServiceEntities = new ArrayList<>();
        if (promotionRequest.getIdServices() != null && !promotionRequest.getIdServices().isEmpty()) {
            for (String idService : promotionRequest.getIdServices()) {
                ServiceEntity serviceEntity = null;
                try{
                    serviceEntity = serviceRepository.findById(idService).get();
                }catch (NoSuchElementException ex){
                    errorDTO.setMessage("Can not find service with id " + idService);
                    errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
                    return errorDTO;
                }

                PromotionServiceEntity promotionServiceEntity = new PromotionServiceEntity();
                promotionServiceEntity.setIdPromotionService(RandomIdUtils.generateRandomId());
                promotionServiceEntity.setServiceEntity(serviceEntity);
                promotionServiceEntity.setCreated_at(LocalDateTime.now());
                promotionServiceEntities.add(promotionServiceEntity);
            }
        }
        promotionsEntity.setPromotionServiceEntities(promotionServiceEntities);
        promotionsEntity.setUpdated_at(LocalDateTime.now());
        promotionsEntity.setPromotionStatus(promotionRequest.getPromotionStatus());
        promotionsRepository.save(promotionsEntity);
        messageResponse.setMessage("Success");
        messageResponse.setHttpStatus(HttpStatus.OK);
        return messageResponse;
    }
}
