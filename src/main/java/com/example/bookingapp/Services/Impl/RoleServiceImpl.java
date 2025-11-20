package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.RoleEntity;
import com.example.bookingapp.Models.DTO.RoleDTO;
import com.example.bookingapp.Repository.RoleRepository;
import com.example.bookingapp.Services.RoleService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.ArrayList;
import java.util.List;

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
}
