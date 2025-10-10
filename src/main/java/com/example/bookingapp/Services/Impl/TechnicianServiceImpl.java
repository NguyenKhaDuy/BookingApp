package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.*;
import com.example.bookingapp.Models.DTO.LocationTechnicianDTO;
import com.example.bookingapp.Models.DTO.RoleDTO;
import com.example.bookingapp.Models.DTO.TechnicicanDTO;
import com.example.bookingapp.Models.Request.SearchByLocationRequest;
import com.example.bookingapp.Repository.TechnicianRepository;
import com.example.bookingapp.Services.TechnicianService;
import com.example.bookingapp.Utils.ConvertByteToBase64;
import com.example.bookingapp.Utils.ConvertEntityToDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TechnicianServiceImpl implements TechnicianService {
    @Autowired
    TechnicianRepository technicianRepository;
    @Autowired
    ModelMapper modelMapper;
    @Override
    public Page<TechnicicanDTO> getAll(Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        Page<TechnicianEntity> technicianEntities = technicianRepository.findAll(pageable);
        List<TechnicicanDTO> technicicanDTOS = new ArrayList<>();
        for (TechnicianEntity technicianEntity : technicianEntities) {
            //Chuyển từ entity sang dto bằng hàm
            TechnicicanDTO technicicanDTO = ConvertEntityToDTO.ToTechnicianDTO(technicianEntity);
            technicicanDTO.setAvatarBase64(ConvertByteToBase64.toBase64(technicianEntity.getAvatar()));
            technicicanDTO.setLevel(technicianEntity.getLevelEntity().getLevel());

            //Vòng lặp để lấy ra danh sách dịch vụ mà thợ có tham gia
            for (ServiceEntity serviceEntity : technicianEntity.getServiceEntities()) {
                technicicanDTO.getNameServiceTechnician().add(serviceEntity.getName_service());
            }

            //Vòng lặp để lấy ra các vị trí thợ hoạt động
            for (LocationEntity locationEntity : technicianEntity.getLocationEntities()) {
                LocationTechnicianDTO locationTechnicianDTO = new LocationTechnicianDTO();
                locationTechnicianDTO.setWard(locationEntity.getWard());
                locationTechnicianDTO.setDistrict(locationEntity.getDistrict());
                locationTechnicianDTO.setConscious(locationEntity.getConscious());
                technicicanDTO.getLocationTechnicianDTOS().add(locationTechnicianDTO);
            }

            //Vòng lặp xử lí để lấy ra các kĩ năng của thợ
            for (SkillEntity skillEntity : technicianEntity.getSkillEntities()) {
                technicicanDTO.getNameSkillTechnician().add(skillEntity.getSkill_name());
            }
            technicicanDTOS.add(technicicanDTO);

            //Vòng lăp xử lí để lấy ra các role của user
            for (RoleEntity roleEntity : technicianEntity.getRoleEntities()){
                RoleDTO roleDTO = new RoleDTO();
                roleDTO.setId_role(roleEntity.getId_role());
                roleDTO.setRole_name(roleEntity.getRole_name());
                technicicanDTO.getRoleDTOS().add(roleDTO);
            }
        }
        return new PageImpl<>(technicicanDTOS, technicianEntities.getPageable(), technicianEntities.getTotalPages());
    }

    @Override
    public Page<TechnicicanDTO> searchTechnicianByLocation(Integer pageNo, SearchByLocationRequest searchByLocationRequest) {
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        Page<TechnicianEntity> technicianEntities = technicianRepository.findByLocation(searchByLocationRequest, pageable);
        List<TechnicicanDTO> technicicanDTOS = new ArrayList<>();
        for (TechnicianEntity technicianEntity : technicianEntities) {
            //Chuyển từ entity sang dto bằng hàm
            TechnicicanDTO technicicanDTO = ConvertEntityToDTO.ToTechnicianDTO(technicianEntity);
            technicicanDTO.setAvatarBase64(ConvertByteToBase64.toBase64(technicianEntity.getAvatar()));
            technicicanDTO.setLevel(technicianEntity.getLevelEntity().getLevel());

            //Vòng lặp để lấy ra danh sách dịch vụ mà thợ có tham gia
            for (ServiceEntity serviceEntity : technicianEntity.getServiceEntities()) {
                technicicanDTO.getNameServiceTechnician().add(serviceEntity.getName_service());
            }

            //Vòng lặp để lấy ra các vị trí thợ hoạt động
            for (LocationEntity locationEntity : technicianEntity.getLocationEntities()) {
                LocationTechnicianDTO locationTechnicianDTO = new LocationTechnicianDTO();
                locationTechnicianDTO.setWard(locationEntity.getWard());
                locationTechnicianDTO.setDistrict(locationEntity.getDistrict());
                locationTechnicianDTO.setConscious(locationEntity.getConscious());
                technicicanDTO.getLocationTechnicianDTOS().add(locationTechnicianDTO);
            }

            //Vòng lặp xử lí để lấy ra các kĩ năng của thợ
            for (SkillEntity skillEntity : technicianEntity.getSkillEntities()) {
                technicicanDTO.getNameSkillTechnician().add(skillEntity.getSkill_name());
            }
            technicicanDTOS.add(technicicanDTO);

            //Vòng lăp xử lí để lấy ra các role của user
            for (RoleEntity roleEntity : technicianEntity.getRoleEntities()){
                RoleDTO roleDTO = new RoleDTO();
                roleDTO.setId_role(roleEntity.getId_role());
                roleDTO.setRole_name(roleEntity.getRole_name());
                technicicanDTO.getRoleDTOS().add(roleDTO);
            }
        }
        return new PageImpl<>(technicicanDTOS, technicianEntities.getPageable(), technicianEntities.getTotalPages());
    }
}
