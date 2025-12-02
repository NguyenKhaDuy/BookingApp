package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.LevelDTO;
import com.example.bookingapp.Models.DTO.RoleDTO;
import com.example.bookingapp.Models.Request.LevelRequest;
import com.example.bookingapp.Models.Request.RoleRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface LevelService {
    Page<LevelDTO> getAll(Integer pageNo);
    Object detailLevel(Long id_level);
    Object createLevel(LevelRequest levelRequest);
    Object updateLevel(LevelRequest levelRequest);
    Object deleteLevel(Long id_level);
}
