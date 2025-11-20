package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.RoleDTO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface RoleService {
    Page<RoleDTO> getAll(Integer pageNo);
}
