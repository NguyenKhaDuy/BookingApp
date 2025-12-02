package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.LevelEntity;
import com.example.bookingapp.Entity.RoleEntity;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.LevelDTO;
import com.example.bookingapp.Models.DTO.MessageDTO;
import com.example.bookingapp.Models.DTO.RoleDTO;
import com.example.bookingapp.Models.Request.LevelRequest;
import com.example.bookingapp.Repository.LevelRepository;
import com.example.bookingapp.Services.LevelService;
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
public class LevelServiceImpl implements LevelService {
    @Autowired
    LevelRepository levelRepository;
    @Autowired
    ModelMapper modelMapper;

    @Override
    public Page<LevelDTO> getAll(Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        Page<LevelEntity> levelEntities  = levelRepository.findAll(pageable);
        List<LevelDTO> levelDTOS = new ArrayList<>();
        for (LevelEntity levelEntity : levelEntities){
            LevelDTO levelDTO = new LevelDTO();
            modelMapper.map(levelDTO, levelEntity);
            levelDTOS.add(levelDTO);
        }
        return new PageImpl<>(levelDTOS, levelEntities.getPageable(), levelEntities.getTotalElements());
    }

    @Override
    public Object detailLevel(Long id_level) {
        ErrorDTO errorDTO = new ErrorDTO();
        try{
            LevelEntity levelEntity = levelRepository.findById(id_level).get();
            LevelDTO levelDTO = new LevelDTO();
            modelMapper.map(levelEntity, levelDTO);
            return levelDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found level");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object createLevel(LevelRequest levelRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageDTO messageDTO = new MessageDTO();
        try {
            LevelEntity levelEntity = new LevelEntity();
            modelMapper.map(levelRequest, levelEntity);
            levelEntity.setCreated_at(LocalDateTime.now());
            levelEntity.setUpdated_at(LocalDateTime.now());
            levelRepository.save(levelEntity);
            messageDTO.setMessage("Success");
            messageDTO.setHttpStatus(HttpStatus.OK);
            return messageDTO;
        }catch (Exception ex){
            errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
            errorDTO.setMessage("Fail");
            return errorDTO;
        }
    }

    @Override
    public Object updateLevel(LevelRequest levelRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageDTO messageDTO = new MessageDTO();
        try {
            LevelEntity levelEntity = levelRepository.findById(levelRequest.getId_level()).get();
            modelMapper.map(levelRequest, levelEntity);
            levelEntity.setUpdated_at(LocalDateTime.now());
            messageDTO.setMessage("Success");
            messageDTO.setHttpStatus(HttpStatus.OK);
            return messageDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found level");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object deleteLevel(Long id_level) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageDTO messageDTO = new MessageDTO();
        try {
            LevelEntity levelEntity = levelRepository.findById(id_level).get();
            levelRepository.delete(levelEntity);
            messageDTO.setMessage("Success");
            messageDTO.setHttpStatus(HttpStatus.OK);
            return messageDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found level");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }
}
