package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.SkillEntity;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.Response.MessageResponse;
import com.example.bookingapp.Models.DTO.SkillDTO;
import com.example.bookingapp.Models.Request.SkillRequest;
import com.example.bookingapp.Repository.SkillRepository;
import com.example.bookingapp.Services.SkillService;
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
public class SkillServiceImpl implements SkillService {
    @Autowired
    SkillRepository skillRepository;
    @Autowired
    ModelMapper modelMapper;
    @Override
    public Page<SkillDTO> getAll(Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        Page<SkillEntity> skillEntities = skillRepository.findAll(pageable);
        List<SkillDTO> skillDTOS = new ArrayList<>();
        for (SkillEntity skillEntity : skillEntities){
            SkillDTO skillDTO = new SkillDTO();
            modelMapper.map(skillEntity, skillDTO);
            skillDTOS.add(skillDTO);
        }
        return new PageImpl<>(skillDTOS, skillEntities.getPageable(), skillEntities.getTotalElements());
    }

    @Override
    public Object detailSkill(Long id_skill) {
        ErrorDTO errorDTO = new ErrorDTO();
        try{
            SkillEntity skillEntity = skillRepository.findById(id_skill).get();
            SkillDTO skillDTO = new SkillDTO();
            modelMapper.map(skillEntity, skillDTO);
            return skillDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found skill");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object createSkill(SkillRequest skillRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageResponse messageResponse = new MessageResponse();
        try {
            SkillEntity skillEntity = new SkillEntity();
            skillEntity.setSkill_name(skillRequest.getSkill_name());
            skillEntity.setCreated_at(LocalDateTime.now());
            skillEntity.setUpdated_at(LocalDateTime.now());
            skillRepository.save(skillEntity);
            messageResponse.setMessage("Success");
            messageResponse.setHttpStatus(HttpStatus.OK);
            return messageResponse;
        }catch (Exception ex){
            errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
            errorDTO.setMessage("Fail");
            return errorDTO;
        }
    }

    @Override
    public Object updateSkill(SkillRequest skillRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageResponse messageResponse = new MessageResponse();
        try {
            SkillEntity skillEntity = skillRepository.findById(skillRequest.getId_skill()).get();
            modelMapper.map(skillRequest, skillEntity);
            skillEntity.setUpdated_at(LocalDateTime.now());
            skillRepository.save(skillEntity);
            messageResponse.setMessage("Success");
            messageResponse.setHttpStatus(HttpStatus.OK);
            return messageResponse;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found skill");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object deleteSkill(Long id_skill) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageResponse messageResponse = new MessageResponse();
        try {
            SkillEntity skillEntity = skillRepository.findById(id_skill).get();
            skillRepository.delete(skillEntity);
            messageResponse.setMessage("Success");
            messageResponse.setHttpStatus(HttpStatus.OK);
            return messageResponse;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found skill");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }
}
