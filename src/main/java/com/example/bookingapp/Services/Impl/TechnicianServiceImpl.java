package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.*;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.LocationTechnicianDTO;
import com.example.bookingapp.Models.DTO.RoleDTO;
import com.example.bookingapp.Models.DTO.TechnicicanDTO;
import com.example.bookingapp.Models.Request.SearchByLocationRequest;
import com.example.bookingapp.Repository.ServiceRepository;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TechnicianServiceImpl implements TechnicianService {
    @Autowired
    TechnicianRepository technicianRepository;
    @Autowired
    ServiceRepository serviceRepository;
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
            technicicanDTOS.add(technicicanDTO);            //Vòng lăp xử lí để lấy ra các role của user


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
    public Object getById(String id_technician) {
        TechnicicanDTO technicicanDTO;
        try{
            TechnicianEntity technicianEntity = technicianRepository.findById(id_technician).get();
            //Chuyển từ entity sang DTO
             technicicanDTO = ConvertEntityToDTO.ToTechnicianDTO(technicianEntity);
            technicicanDTO.setAvatarBase64(ConvertByteToBase64.toBase64(technicianEntity.getAvatar()));
            technicicanDTO.setLevel(technicianEntity.getLevelEntity().getLevel());

            //Vòng lặp xử lí danh sách dịch vụ của thợ có tham gia
            for(ServiceEntity serviceEntity : technicianEntity.getServiceEntities()){
                technicicanDTO.getNameServiceTechnician().add(serviceEntity.getName_service());
            }

            //Vòng lặp xử lí vị trí thợ làm việc
            for(LocationEntity locationEntity : technicianEntity.getLocationEntities()){
                LocationTechnicianDTO locationTechnicianDTO = new LocationTechnicianDTO();
                locationTechnicianDTO.setWard(locationEntity.getWard());
                locationTechnicianDTO.setDistrict(locationEntity.getDistrict());
                locationTechnicianDTO.setConscious(locationEntity.getConscious());
                technicicanDTO.getLocationTechnicianDTOS().add(locationTechnicianDTO);
            }

            //Vòng lặp xử lí kĩ năng của thợ
            for (SkillEntity skillEntity : technicianEntity.getSkillEntities()){
                technicicanDTO.getNameSkillTechnician().add(skillEntity.getSkill_name());
            }

            //Vòng lặp xử lí role
            for(RoleEntity roleEntity : technicianEntity.getRoleEntities()){
                RoleDTO roleDTO = new RoleDTO();
                roleDTO.setId_role(roleEntity.getId_role());
                roleDTO.setRole_name(roleEntity.getRole_name());
                technicicanDTO.getRoleDTOS().add(roleDTO);
            }
        }catch(NoSuchElementException ex){
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setMessage(ex.getMessage());
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
        return technicicanDTO;
    }

    @Override
    public Page<TechnicicanDTO> searchTechnicianByName(Integer pageNo, String name_technician) {
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        Page<TechnicianEntity> technicianEntities = technicianRepository.searchByName(name_technician, pageable);
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
            technicicanDTOS.add(technicicanDTO);            //Vòng lăp xử lí để lấy ra các role của user


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
    public Page<TechnicicanDTO> searchTechnicianByService(Integer pageNo, Long id_service) {
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        List<TechnicicanDTO> technicicanDTOS = new ArrayList<>();
        Page<TechnicianEntity> technicianEntities = null;
        try{
            ServiceEntity serviceEntity = serviceRepository.findById(id_service).get();
            technicianEntities = technicianRepository.findByServiceEntities(serviceEntity, pageable);
            for(TechnicianEntity technicianEntity : technicianEntities){
                TechnicicanDTO technicicanDTO = ConvertEntityToDTO.ToTechnicianDTO(technicianEntity);
                technicicanDTO.setLevel(technicianEntity.getLevelEntity().getLevel());

                //Vòng lặp xử lí dịch vụ của thợ
                for(ServiceEntity service : technicianEntity.getServiceEntities()){
                    technicicanDTO.getNameServiceTechnician().add(service.getName_service());
                }

                //Vòng lặp xử lí vị trí hoạt động của thợ
                for (LocationEntity locationEntity : technicianEntity.getLocationEntities()){
                    LocationTechnicianDTO locationTechnicianDTO = new LocationTechnicianDTO();
                    locationTechnicianDTO.setWard(locationEntity.getWard());
                    locationTechnicianDTO.setDistrict(locationEntity.getDistrict());
                    locationTechnicianDTO.setConscious(locationEntity.getConscious());
                    technicicanDTO.getLocationTechnicianDTOS().add(locationTechnicianDTO);
                }

                //Vòng lặp xử lí kĩ năng thợ
                for(SkillEntity skillEntity : technicianEntity.getSkillEntities()){
                    technicicanDTO.getNameSkillTechnician().add(skillEntity.getSkill_name());
                }

                //Vòng lặp xử lí role
                for(RoleEntity roleEntity : technicianEntity.getRoleEntities()){
                    RoleDTO roleDTO = new RoleDTO();
                    roleDTO.setId_role(roleEntity.getId_role());
                    roleDTO.setRole_name(roleEntity.getRole_name());
                    technicicanDTO.getRoleDTOS().add(roleDTO);
                }
                technicicanDTOS.add(technicicanDTO);
            }
        }catch (NoSuchElementException ex){
            ex.printStackTrace();
            return null;
        }
        return new PageImpl<>(technicicanDTOS, technicianEntities.getPageable(), technicianEntities.getTotalElements());
    }
}
