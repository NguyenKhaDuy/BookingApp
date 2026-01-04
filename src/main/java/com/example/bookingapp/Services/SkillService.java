package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.RoleDTO;
import com.example.bookingapp.Models.DTO.SkillDTO;
import com.example.bookingapp.Models.Request.RoleRequest;
import com.example.bookingapp.Models.Request.SkillRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SkillService {
    Page<SkillDTO> getAll(Integer pageNo);
    List<SkillDTO> getAll();
    Object detailSkill(Long id_skill);
    Object createSkill(SkillRequest skillRequest);
    Object updateSkill(SkillRequest skillRequest);
    Object deleteSkill(Long id_skill);
}
