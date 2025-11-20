package com.example.bookingapp.Services;

import com.example.bookingapp.Entity.TechnicianEntity;
import com.example.bookingapp.Models.DTO.LocationDTO;
import com.example.bookingapp.Models.DTO.RepairRequestDTO;
import com.example.bookingapp.Models.DTO.SkillDTO;
import com.example.bookingapp.Models.DTO.TechnicicanDTO;
import com.example.bookingapp.Models.Request.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface TechnicianService {
    Page<TechnicicanDTO> getAll(Integer pageNo);
    Page<TechnicicanDTO> searchTechnicianByLocation(Integer pageNo, SearchByLocationRequest searchByLocationRequest);
    Object getById(String id_technician);
    Page<TechnicicanDTO> searchTechnicianByName(Integer pageNo, String name_technician);
    Page<TechnicicanDTO> searchTechnicianByService(Integer pageNo, Long id_service);
    Object updateProfile(TechnicianProfileRequest technicianProfileRequest);
    Object updateAvatar(AvatarRequest avatarRequest);
    Object addSkill(SkillTechnicianRequest skillTechnicianRequest);
    Page<SkillDTO> getSkill(String id_user, Integer pageNo);
    Object deleteSkillOfTechnician(SkillTechnicianRequest skillTechnicianRequest);
    Object addLocation(LocationTechnicianRequest locationTechnicianRequest);
    Page<LocationDTO> getLocation(String id_user, Integer pageNo);
    Object deleteLocationOfTechnician(LocationTechnicianRequest locationTechnicianRequest);
    Object getWalletOfTechnician(WalletRequest walletRequest);
    Object deleteTechnician(String id_technician);
}
