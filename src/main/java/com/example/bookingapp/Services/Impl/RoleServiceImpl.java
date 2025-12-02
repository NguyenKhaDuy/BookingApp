package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.RoleEntity;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.MessageDTO;
import com.example.bookingapp.Models.DTO.RoleDTO;
import com.example.bookingapp.Models.Request.RoleRequest;
import com.example.bookingapp.Repository.RoleRepository;
import com.example.bookingapp.Services.RoleService;
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
public class RoleServiceImpl implements RoleService {
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    ModelMapper modelMapper;
    @Override
    public Page<RoleDTO> getAll(Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        Page<RoleEntity> roleEntities = roleRepository.findAll(pageable);
        List<RoleDTO> roleDTOS = new ArrayList<>();
        for (RoleEntity roleEntity : roleEntities){
            RoleDTO roleDTO = new RoleDTO();
            modelMapper.map(roleEntity, roleDTO);
            roleDTOS.add(roleDTO);
        }
        return new PageImpl<>(roleDTOS, roleEntities.getPageable(), roleEntities.getTotalElements());
    }

    @Override
    public Object detailRole(Long id_role) {
        ErrorDTO errorDTO = new ErrorDTO();
        try{
            RoleEntity roleEntity = roleRepository.findById(id_role).get();
            RoleDTO roleDTO = new RoleDTO();
            modelMapper.map(roleEntity, roleDTO);
            return roleDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found role");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object createRole(RoleRequest roleRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageDTO messageDTO = new MessageDTO();
        try {
            RoleEntity roleEntity = new RoleEntity();
            roleEntity.setRole_name(roleRequest.getRole_name());
            roleEntity.setCreated_at(LocalDateTime.now());
            roleEntity.setUpdated_at(LocalDateTime.now());
            roleRepository.save(roleEntity);
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
    public Object updateRole(RoleRequest roleRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageDTO messageDTO = new MessageDTO();
        try {
            RoleEntity roleEntity = roleRepository.findById(roleRequest.getId_role()).get();
            modelMapper.map(roleRequest, roleEntity);
            roleEntity.setUpdated_at(LocalDateTime.now());
            messageDTO.setMessage("Success");
            messageDTO.setHttpStatus(HttpStatus.OK);
            return messageDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found role");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object deleteRole(Long id_role) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageDTO messageDTO = new MessageDTO();
        try {
            RoleEntity roleEntity = roleRepository.findById(id_role).get();
            roleRepository.delete(roleEntity);
            messageDTO.setMessage("Success");
            messageDTO.setHttpStatus(HttpStatus.OK);
            return messageDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found role");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }
}
