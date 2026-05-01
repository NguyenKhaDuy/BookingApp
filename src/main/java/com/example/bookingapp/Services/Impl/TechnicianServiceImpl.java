package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.*;
import com.example.bookingapp.Models.DTO.*;
import com.example.bookingapp.Models.Request.*;
import com.example.bookingapp.Models.Response.MessageResponse;
import com.example.bookingapp.Repository.*;
import com.example.bookingapp.Services.TechnicianService;
import com.example.bookingapp.Services.WebSocketService;
import com.example.bookingapp.Utils.ConvertByteToBase64;
import com.example.bookingapp.Utils.ConvertEntityToDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class TechnicianServiceImpl implements TechnicianService {
    @Autowired
    TechnicianRepository technicianRepository;
    @Autowired
    ServiceRepository serviceRepository;
    @Autowired
    RatingRepository ratingRepository;
    @Autowired
    SkillRepository skillRepository;
    @Autowired
    LocationRepository locationRepository;
    @Autowired
    TechnicianScheduleRepository technicianScheduleRepository;
    @Autowired
    StatusRepository statusRepository;
    @Autowired
    TechnicianWalletRepository technicianWalletRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    RepairRequestRepository repairRequestRepository;
    @Autowired
    InvoicesRepository invoicesRepository;
    @Autowired
    WebSocketService webSocketService;
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    NotificationTypeRepository notificationTypeRepository;

    public Integer average_star_tecnician(List<RatingEntity> ratingEntities){
        float average_star = 0;
        Integer sum_star = 0;
        for (RatingEntity ratingEntity : ratingEntities){
            sum_star += ratingEntity.getStars();
        }
        if (ratingEntities.size() > 0){
            average_star = (float) (sum_star / ratingEntities.size());
        }
        return Math.round(average_star);
    }

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

            //Tính số sao trung bình của một thợ
            List<RatingEntity> ratingEntities = ratingRepository.findByTechnicianEntity(technicianEntity);
            if (ratingEntities != null){
                Integer average_star = average_star_tecnician(ratingEntities);
                technicicanDTO.setTotal_star(average_star);
            }else {
                technicicanDTO.setTotal_star(0);
            }


            //Vòng lặp để lấy ra danh sách dịch vụ mà thợ có tham gia
            for(ServiceEntity serviceEntity : technicianEntity.getServiceEntities()){
                TechnicianServiceDTO technicianServiceDTO = new TechnicianServiceDTO();
                technicianServiceDTO.setName_service(serviceEntity.getName_service());
                technicianServiceDTO.setIcon(ConvertByteToBase64.toBase64(serviceEntity.getIcon()));
                technicicanDTO.getTechnicianServiceDTOS().add(technicianServiceDTO);
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

            //Vòng lăp xử lí để lấy ra các role của user
            for (RoleEntity roleEntity : technicianEntity.getRoleEntities()){
                RoleDTO roleDTO = new RoleDTO();
                roleDTO.setId_role(roleEntity.getId_role());
                roleDTO.setRole_name(roleEntity.getRoleName());
                technicicanDTO.getRoleDTOS().add(roleDTO);
            }

            for(RatingEntity ratingEntity : technicianEntity.getRatingEntities()){
                RatingDTO ratingDTO = new RatingDTO();
                modelMapper.map(ratingEntity, ratingDTO);
                ratingDTO.setId_user(ratingEntity.getCustomerEntity().getId_user());
                ratingDTO.setFull_name(ratingEntity.getCustomerEntity().getFull_name());
                ratingDTO.setAvatarBase64(ConvertByteToBase64.toBase64(ratingEntity.getCustomerEntity().getAvatar()));
                technicicanDTO.getRatingDTOS().add(ratingDTO);
            }

//            Tìm kiếm lịch của thợ để lấy trạng thái hiện tại của thợ
            List<TechnicianScheduleEntity> technicianScheduleEntities = technicianScheduleRepository.findByTechnicianEntityAndDateOrderByIdScheduleDesc(technicianEntity, LocalDate.now());
            if(technicianScheduleEntities.size() > 0){
                for (TechnicianScheduleEntity technicianScheduleEntity : technicianScheduleEntities){
                    if (technicianScheduleEntity.getStatusEntity().getNameStatus().equals("ONLINE")){
                        technicicanDTO.setStatus_technician("ONLINE");
                    }
                }
            }else{
                technicicanDTO.setStatus_technician("OFFLINE");
            }

            technicicanDTOS.add(technicicanDTO);
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

            //Tính số sao trung bình của một thợ
            List<RatingEntity> ratingEntities = ratingRepository.findByTechnicianEntity(technicianEntity);
            Integer average_star = average_star_tecnician(ratingEntities);
            technicicanDTO.setTotal_star(average_star);

            //Vòng lặp để lấy ra danh sách dịch vụ mà thợ có tham gia
            //Vòng lặp xử lí danh sách dịch vụ của thợ có tham gia
            for(ServiceEntity serviceEntity : technicianEntity.getServiceEntities()){
                TechnicianServiceDTO technicianServiceDTO = new TechnicianServiceDTO();
                technicianServiceDTO.setName_service(serviceEntity.getName_service());
                technicianServiceDTO.setIcon(ConvertByteToBase64.toBase64(serviceEntity.getIcon()));
                technicicanDTO.getTechnicianServiceDTOS().add(technicianServiceDTO);
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

            //Vòng lăp xử lí để lấy ra các role của user
            for (RoleEntity roleEntity : technicianEntity.getRoleEntities()){
                RoleDTO roleDTO = new RoleDTO();
                roleDTO.setId_role(roleEntity.getId_role());
                roleDTO.setRole_name(roleEntity.getRoleName());
                technicicanDTO.getRoleDTOS().add(roleDTO);
            }

            //Lấy ra lịch làm việc của thợ
            for (TechnicianScheduleEntity technicianScheduleEntity : technicianEntity.getTechnicianScheduleEntityList()){
                TechnicianScheduleDTO technicianScheduleDTO = new TechnicianScheduleDTO();
                modelMapper.map(technicianScheduleEntity, technicianScheduleDTO);
                technicianScheduleDTO.setStatus_code(technicianScheduleEntity.getStatusEntity().getNameStatus());
                technicianScheduleDTO.setId_technician(technicianScheduleEntity.getTechnicianEntity().getId_user());
                technicicanDTO.getTechnicianScheduleDTOS().add(technicianScheduleDTO);
            }

            for(RatingEntity ratingEntity : technicianEntity.getRatingEntities()){
                RatingDTO ratingDTO = new RatingDTO();
                modelMapper.map(ratingEntity, ratingDTO);
                ratingDTO.setId_user(ratingEntity.getCustomerEntity().getId_user());
                ratingDTO.setFull_name(ratingEntity.getCustomerEntity().getFull_name());
                ratingDTO.setAvatarBase64(ConvertByteToBase64.toBase64(ratingEntity.getCustomerEntity().getAvatar()));
                technicicanDTO.getRatingDTOS().add(ratingDTO);
            }

//            Tìm kiếm lịch của thợ để lấy trạng thái hiện tại của thợ
            List<TechnicianScheduleEntity> technicianScheduleEntities = technicianScheduleRepository.findByTechnicianEntityAndDateOrderByIdScheduleDesc(technicianEntity, LocalDate.now());
            if(technicianScheduleEntities.size() > 0){
                for (TechnicianScheduleEntity technicianScheduleEntity : technicianScheduleEntities){
                    if (technicianScheduleEntity.getStatusEntity().getNameStatus().equals("ONLINE")){
                        technicicanDTO.setStatus_technician("ONLINE");
                    }
                }
            }else{
                technicicanDTO.setStatus_technician("OFFLINE");
            }

            technicicanDTOS.add(technicicanDTO);
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

            //Tính số sao trung bình của một thợ
            List<RatingEntity> ratingEntities = ratingRepository.findByTechnicianEntity(technicianEntity);
            Integer average_star = average_star_tecnician(ratingEntities);
            technicicanDTO.setTotal_star(average_star);

            //Vòng lặp xử lí danh sách dịch vụ của thợ có tham gia
            for(ServiceEntity serviceEntity : technicianEntity.getServiceEntities()){
                TechnicianServiceDTO technicianServiceDTO = new TechnicianServiceDTO();
                technicianServiceDTO.setName_service(serviceEntity.getName_service());
                technicianServiceDTO.setIcon(ConvertByteToBase64.toBase64(serviceEntity.getIcon()));
                technicicanDTO.getTechnicianServiceDTOS().add(technicianServiceDTO);
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
                roleDTO.setRole_name(roleEntity.getRoleName());
                technicicanDTO.getRoleDTOS().add(roleDTO);
            }

            //Lấy ra lịch làm việc của thợ
            for (TechnicianScheduleEntity technicianScheduleEntity : technicianEntity.getTechnicianScheduleEntityList()){
                TechnicianScheduleDTO technicianScheduleDTO = new TechnicianScheduleDTO();
                modelMapper.map(technicianScheduleEntity, technicianScheduleDTO);
                technicianScheduleDTO.setStatus_code(technicianScheduleEntity.getStatusEntity().getNameStatus());
                technicianScheduleDTO.setId_technician(technicianScheduleEntity.getTechnicianEntity().getId_user());
                technicicanDTO.getTechnicianScheduleDTOS().add(technicianScheduleDTO);
            }

            for(RatingEntity ratingEntity : technicianEntity.getRatingEntities()){
                RatingDTO ratingDTO = new RatingDTO();
                modelMapper.map(ratingEntity, ratingDTO);
                ratingDTO.setId_user(ratingEntity.getCustomerEntity().getId_user());
                ratingDTO.setFull_name(ratingEntity.getCustomerEntity().getFull_name());
                ratingDTO.setAvatarBase64(ConvertByteToBase64.toBase64(ratingEntity.getCustomerEntity().getAvatar()));
                technicicanDTO.getRatingDTOS().add(ratingDTO);
            }

//            Tìm kiếm lịch của thợ để lấy trạng thái hiện tại của thợ
            List<TechnicianScheduleEntity> technicianScheduleEntities = technicianScheduleRepository.findByTechnicianEntityAndDateOrderByIdScheduleDesc(technicianEntity, LocalDate.now());
            if(technicianScheduleEntities.size() > 0){
                for (TechnicianScheduleEntity technicianScheduleEntity : technicianScheduleEntities){
                    if (technicianScheduleEntity.getStatusEntity().getNameStatus().equals("ONLINE")){
                        technicicanDTO.setStatus_technician("ONLINE");
                    }
                }
            }else{
                technicicanDTO.setStatus_technician("OFFLINE");
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

            //Tính số sao trung bình của một thợ
            List<RatingEntity> ratingEntities = ratingRepository.findByTechnicianEntity(technicianEntity);
            Integer average_star = average_star_tecnician(ratingEntities);
            technicicanDTO.setTotal_star(average_star);

            //Vòng lặp để lấy ra danh sách dịch vụ mà thợ có tham gia
            for(ServiceEntity serviceEntity : technicianEntity.getServiceEntities()){
                TechnicianServiceDTO technicianServiceDTO = new TechnicianServiceDTO();
                technicianServiceDTO.setName_service(serviceEntity.getName_service());
                technicianServiceDTO.setIcon(ConvertByteToBase64.toBase64(serviceEntity.getIcon()));
                technicicanDTO.getTechnicianServiceDTOS().add(technicianServiceDTO);
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

            //Vòng lăp xử lí để lấy ra các role của user
           for (RoleEntity roleEntity : technicianEntity.getRoleEntities()){
                RoleDTO roleDTO = new RoleDTO();
                roleDTO.setId_role(roleEntity.getId_role());
                roleDTO.setRole_name(roleEntity.getRoleName());
                technicicanDTO.getRoleDTOS().add(roleDTO);
            }

            //Lấy ra lịch làm việc của thợ
            for (TechnicianScheduleEntity technicianScheduleEntity : technicianEntity.getTechnicianScheduleEntityList()){
                TechnicianScheduleDTO technicianScheduleDTO = new TechnicianScheduleDTO();
                modelMapper.map(technicianScheduleEntity, technicianScheduleDTO);
                technicianScheduleDTO.setStatus_code(technicianScheduleEntity.getStatusEntity().getNameStatus());
                technicianScheduleDTO.setId_technician(technicianScheduleEntity.getTechnicianEntity().getId_user());
                technicicanDTO.getTechnicianScheduleDTOS().add(technicianScheduleDTO);
            }

            for(RatingEntity ratingEntity : technicianEntity.getRatingEntities()){
                RatingDTO ratingDTO = new RatingDTO();
                modelMapper.map(ratingEntity, ratingDTO);
                ratingDTO.setId_user(ratingEntity.getCustomerEntity().getId_user());
                ratingDTO.setFull_name(ratingEntity.getCustomerEntity().getFull_name());
                ratingDTO.setAvatarBase64(ConvertByteToBase64.toBase64(ratingEntity.getCustomerEntity().getAvatar()));
                technicicanDTO.getRatingDTOS().add(ratingDTO);
            }

            //            Tìm kiếm lịch của thợ để lấy trạng thái hiện tại của thợ
            List<TechnicianScheduleEntity> technicianScheduleEntities = technicianScheduleRepository.findByTechnicianEntityAndDateOrderByIdScheduleDesc(technicianEntity, LocalDate.now());
            if(technicianScheduleEntities.size() > 0){
                for (TechnicianScheduleEntity technicianScheduleEntity : technicianScheduleEntities){
                    if (technicianScheduleEntity.getStatusEntity().getNameStatus().equals("ONLINE")){
                        technicicanDTO.setStatus_technician("ONLINE");
                    }
                }
            }else{
                technicicanDTO.setStatus_technician("OFFLINE");
            }

            technicicanDTOS.add(technicicanDTO);
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

                //Tính số sao trung bình của một thợ
                List<RatingEntity> ratingEntities = ratingRepository.findByTechnicianEntity(technicianEntity);
                Integer average_star = average_star_tecnician(ratingEntities);
                technicicanDTO.setTotal_star(average_star);

                //Vòng lặp xử lí danh sách dịch vụ của thợ có tham gia
                for(ServiceEntity service : technicianEntity.getServiceEntities()){
                    TechnicianServiceDTO technicianServiceDTO = new TechnicianServiceDTO();
                    technicianServiceDTO.setName_service(service.getName_service());
                    technicianServiceDTO.setIcon(ConvertByteToBase64.toBase64(service.getIcon()));
                    technicicanDTO.getTechnicianServiceDTOS().add(technicianServiceDTO);
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
                    roleDTO.setRole_name(roleEntity.getRoleName());
                    technicicanDTO.getRoleDTOS().add(roleDTO);
                }

                for(RatingEntity ratingEntity : technicianEntity.getRatingEntities()){
                    RatingDTO ratingDTO = new RatingDTO();
                    modelMapper.map(ratingEntity, ratingDTO);
                    ratingDTO.setId_user(ratingEntity.getCustomerEntity().getId_user());
                    ratingDTO.setFull_name(ratingEntity.getCustomerEntity().getFull_name());
                    ratingDTO.setAvatarBase64(ConvertByteToBase64.toBase64(ratingEntity.getCustomerEntity().getAvatar()));
                    technicicanDTO.getRatingDTOS().add(ratingDTO);
                }

                //Lấy ra lịch làm việc của thợ
                for (TechnicianScheduleEntity technicianScheduleEntity : technicianEntity.getTechnicianScheduleEntityList()){
                    TechnicianScheduleDTO technicianScheduleDTO = new TechnicianScheduleDTO();
                    modelMapper.map(technicianScheduleEntity, technicianScheduleDTO);
                    technicianScheduleDTO.setStatus_code(technicianScheduleEntity.getStatusEntity().getNameStatus());
                    technicianScheduleDTO.setId_technician(technicianScheduleEntity.getTechnicianEntity().getId_user());
                    technicicanDTO.getTechnicianScheduleDTOS().add(technicianScheduleDTO);
                }

                //Tìm kiếm lịch của thợ để lấy trạng thái hiện tại của thợ
                List<TechnicianScheduleEntity> technicianScheduleEntities = technicianScheduleRepository.findByTechnicianEntityAndDateOrderByIdScheduleDesc(technicianEntity, LocalDate.now());
                if(technicianScheduleEntities.size() > 0){
                    for (TechnicianScheduleEntity technicianScheduleEntity : technicianScheduleEntities){
                        if (technicianScheduleEntity.getStatusEntity().getNameStatus().equals("ONLINE")){
                            technicicanDTO.setStatus_technician("ONLINE");
                        }
                    }
                }else{
                    technicicanDTO.setStatus_technician("OFFLINE");
                }

                technicicanDTOS.add(technicicanDTO);
            }
        }catch (NoSuchElementException ex){
            ex.printStackTrace();
            return null;
        }
        return new PageImpl<>(technicicanDTOS, technicianEntities.getPageable(), technicianEntities.getTotalElements());
    }

    @Override
    public Object updateProfile(TechnicianProfileRequest technicianProfileRequest) {
        MessageResponse messageResponse = new MessageResponse();
        ErrorDTO errorDTO = new ErrorDTO();
        try{
            TechnicianEntity technicianEntity = technicianRepository.findById(technicianProfileRequest.getId_user()).get();
            try{
                modelMapper.map(technicianProfileRequest, technicianEntity);
                technicianEntity.setUpdated_at(LocalDateTime.now());
                technicianRepository.save(technicianEntity);
                messageResponse.setMessage("Success");
                messageResponse.setHttpStatus(HttpStatus.OK);
                return messageResponse;
            }catch (RuntimeException ex){
                errorDTO.setMessage(ex.getMessage());
                errorDTO.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                return errorDTO;
            }
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found user");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object updateAvatar(AvatarRequest avatarRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageResponse messageResponse = new MessageResponse();
        try{
            TechnicianEntity technicianEntity = technicianRepository.findById(avatarRequest.getId_user()).get();
            try {
                technicianEntity.setAvatar(avatarRequest.getAvatar().getBytes());
                technicianEntity.setUpdated_at(LocalDateTime.now());
            } catch (IOException e) {
                errorDTO.setMessage("Can not convert mutipartfile to byte");
                errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
                return errorDTO;
            }
            technicianRepository.save(technicianEntity);
            messageResponse.setMessage("Success");
            messageResponse.setHttpStatus(HttpStatus.OK);
            return messageResponse;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found user");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object addSkill(SkillTechnicianRequest skillTechnicianRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageResponse messageResponse = new MessageResponse();
        SkillEntity skillEntity = null;
        try {
            //Tìm kiếm thợ
            TechnicianEntity technicianEntity = technicianRepository.findById(skillTechnicianRequest.getId_user()).get();
            try {
                //tìm kiếm kĩ năng
                skillEntity = skillRepository.findById(skillTechnicianRequest.getId_skill()).get();
            }catch (NoSuchElementException ex){
                errorDTO.setMessage("Can not found skill");
                errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
                return errorDTO;
            }
            //thêm kĩ năng và thợ vào từng entity
            technicianEntity.getSkillEntities().add(skillEntity);
            skillEntity.getTechnicianEntities().add(technicianEntity);
            //lưu lại
            technicianRepository.save(technicianEntity);
            messageResponse.setMessage("Success");
            messageResponse.setHttpStatus(HttpStatus.OK);
            return messageResponse;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found technician");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public List<SkillDTO> getSkill(String id_user) {
        List<SkillDTO> skillDTOS = new ArrayList<>();
        try {
            TechnicianEntity technicianEntity = technicianRepository.findById(id_user).get();
            List<SkillEntity> skillEntities = skillRepository.findByTechnicianEntities(technicianEntity);
            for (SkillEntity skillEntity : skillEntities){
                SkillDTO skillDTO = new SkillDTO();
                modelMapper.map(skillEntity, skillDTO);
                skillDTOS.add(skillDTO);
            }
            return skillDTOS;
        }catch (NoSuchElementException ex){
            return null;
        }
    }

    @Override
    public Object deleteSkillOfTechnician(SkillTechnicianRequest skillTechnicianRequest) {
        MessageResponse messageResponse = new MessageResponse();
        ErrorDTO errorDTO = new ErrorDTO();
        SkillEntity skillEntity = null;
        try{
            //tìm kiếm thợ
            TechnicianEntity technicianEntity = technicianRepository.findById(skillTechnicianRequest.getId_user()).get();
            try {
                //tìm kiếm kĩ năng
                skillEntity = skillRepository.findById(skillTechnicianRequest.getId_skill()).get();
            }catch (NoSuchElementException ex){
                errorDTO.setMessage("Can not found skill");
                errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
                return errorDTO;
            }
            //kiểm tra xem kĩ năng người dùng gửi lên có nằm trong danh sách kĩ năng của thợ hay không
            if(technicianEntity.getSkillEntities().contains(skillEntity)){
                //tiến hành xóa kĩ năng và thợ ra khỏi danh sách các thợ của kĩ năng
                //và danh sách kĩ năng của thợ
                technicianEntity.getSkillEntities().remove(skillEntity);
                skillEntity.getTechnicianEntities().remove(technicianEntity);
                technicianRepository.save(technicianEntity);
                messageResponse.setMessage("Success");
                messageResponse.setHttpStatus(HttpStatus.OK);
            }else {
                errorDTO.setMessage("Skill not contains in list skill of technician");
                errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
            }
            return messageResponse;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found technician");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object addLocation(LocationTechnicianRequest locationTechnicianRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageResponse messageResponse = new MessageResponse();
        LocationEntity locationEntity = null;
        try {
            TechnicianEntity technicianEntity = technicianRepository.findById(locationTechnicianRequest.getId_user()).get();
            try {
                locationEntity = locationRepository.findById(locationTechnicianRequest.getId_location()).get();
            }catch (NoSuchElementException ex){
                errorDTO.setMessage("Can not found location");
                errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
                return errorDTO;
            }
            technicianEntity.getLocationEntities().add(locationEntity);
            locationEntity.getTechnicianEntities().add(technicianEntity);
            technicianRepository.save(technicianEntity);
            messageResponse.setMessage("Success");
            messageResponse.setHttpStatus(HttpStatus.OK);
            return messageResponse;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found technician");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public List<LocationDTO> getLocation(String id_user) {
        List<LocationDTO> locationDTOS = new ArrayList<>();
        try {
            TechnicianEntity technicianEntity = technicianRepository.findById(id_user).get();
            List<LocationEntity> locationEntities = locationRepository.findByTechnicianEntities(technicianEntity);
            for (LocationEntity locationEntity : locationEntities){
                LocationDTO locationDTO = new LocationDTO();
                modelMapper.map(locationEntity, locationDTO);
                locationDTOS.add(locationDTO);
            }
            return locationDTOS;
        }catch (NoSuchElementException ex){
            return null;
        }
    }

    @Override
    public Object deleteLocationOfTechnician(LocationTechnicianRequest locationTechnicianRequest) {
        MessageResponse messageResponse = new MessageResponse();
        ErrorDTO errorDTO = new ErrorDTO();
        LocationEntity locationEntity = null;
        try{
            TechnicianEntity technicianEntity = technicianRepository.findById(locationTechnicianRequest.getId_user()).get();
            try {
                locationEntity = locationRepository.findById(locationTechnicianRequest.getId_location()).get();
            }catch (NoSuchElementException ex){
                errorDTO.setMessage("Can not found location");
                errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
                return errorDTO;
            }
            if(technicianEntity.getLocationEntities().contains(locationEntity)){
                technicianEntity.getLocationEntities().remove(locationEntity);
                locationEntity.getTechnicianEntities().remove(technicianEntity);
                technicianRepository.save(technicianEntity);
                messageResponse.setMessage("Success");
                messageResponse.setHttpStatus(HttpStatus.OK);
            }else {
                errorDTO.setMessage("Location not contains in list location of technician");
                errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
            }
            return messageResponse;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found technician");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    //Còn xử lí thêm
    @Override
    public Object getWalletOfTechnician(WalletRequest walletRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        try {
            TechnicianEntity technicianEntity = technicianRepository.findById(walletRequest.getId_technician()).get();
            TechnicianWalletEntity technicianWalletEntity = technicianWalletRepository.findByTechnicianEntity(technicianEntity);
            //Chổ này sau này sẽ so sánh bằng mã hóa tạm thời sẽ làm như bình thường
            //Nhập mã code đúng rồi mới cho vào trong ví
            if (walletRequest.getCode().equals(technicianWalletEntity.getCode())){
                //Lấy ra thông tin ví của thợ
                TechnicianWalletDTO technicianWalletDTO = new TechnicianWalletDTO();
                modelMapper.map(technicianWalletEntity, technicianWalletDTO);
                technicianWalletDTO.setTechnician_id(technicianWalletEntity.getTechnicianEntity().getId_user());
                technicianWalletDTO.setTechnician_name(technicianWalletEntity.getTechnicianEntity().getFull_name());
                //Danh sách các tài khoản ngân hàng hàng đã liên kết với ví
                List<LinkBankAccountDTO> linkBankAccountDTOS = new ArrayList<>();
                for (LinkBankAccountEntity linkBankAccountEntity : technicianWalletEntity.getLinkBankAccountEntities()){
                    LinkBankAccountDTO linkBankAccountDTO = new LinkBankAccountDTO();
                    modelMapper.map(linkBankAccountEntity, linkBankAccountDTO);
                    linkBankAccountDTOS.add(linkBankAccountDTO);
                }
                return technicianWalletDTO;
            }else {
                errorDTO.setMessage("Code incorrect");
                errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
                return errorDTO;
            }
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found technician");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object deleteTechnician(String id_technician) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageResponse messageResponse = new MessageResponse();
        try {
            TechnicianEntity technicianEntity = technicianRepository.findById(id_technician).get();
            technicianRepository.delete(technicianEntity);
            messageResponse.setMessage("Success");
            messageResponse.setHttpStatus(HttpStatus.OK);
            return messageResponse;
        }catch (NoSuchElementException ex) {
            errorDTO.setMessage("Can not found technician");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    //id_tech của thợ đã từ chối yêu cầu của khách hàng
    @Override
    public String filterTechnician(LocalTime time, LocalDate date, Long service_id, String id_tech) {
        try {
            ServiceEntity serviceEntity = serviceRepository.findById(service_id).get();
            //lấy ra danh sách thợ có liên quan đến customer yêu cầu
            List<TechnicianEntity> technicianEntities = technicianRepository.findByServiceEntities(serviceEntity);
            List<TechnicianEntity> technicians = new ArrayList<>();
            for (TechnicianEntity technicianEntity : technicianEntities){
                //Kiểm tra danh sách lịch làm việc của thợ xem có lịch online hay không
                for (TechnicianScheduleEntity technicianScheduleEntity : technicianEntity.getTechnicianScheduleEntityList()){
                    if (technicianScheduleEntity.getStatusEntity().getNameStatus().equals("ONLINE")){
                        LocalDate dateSchedule = technicianScheduleEntity.getDate();
                        LocalTime timeStart = technicianScheduleEntity.getTime_start();
                        LocalTime timeEnd = technicianScheduleEntity.getTime_end();
                        //kiểm tra xem ngày giờ hẹn của khách hàng có trong giờ làm của thợ hay không
                        if(dateSchedule.equals(date) && !time.isBefore(timeStart) && !time.isAfter(timeEnd)){
                            //kiểm tra xem thợ có đang bận hay không
                            if (isTechnicianFree(technicianEntity.getId_user())){
                                //kiểm tra hiệu suất của thợ
                                if (technicianEntity.getEfficiency() >= 10 && technicianEntity.getTechnician_debt() < 1000000){
                                    technicians.add(technicianEntity);
                                }
                            }
                        }
                    }
                }
            }

            if (id_tech != null){
                TechnicianEntity technician = technicianRepository.findById(id_tech).get();
                technicians.remove(technician);
            }

            Map<String, Long> technicianEfficiency = new HashMap<>();
            Long maxEfficiency = 10L;
            // Tìm hiệu xuất cao nhất và lưu lại danh sách thợ có hiệu suất đó
            for (TechnicianEntity tech : technicians) {
                Long efficiencyTechnician = tech.getEfficiency();
                String techId = tech.getId_user();
                // Nếu hiệu suất cao hơn max hiện tại → reset map
                if (efficiencyTechnician > maxEfficiency) {
                    maxEfficiency = efficiencyTechnician;
                    technicianEfficiency.clear();  // reset danh sách
                    technicianEfficiency.put(techId, efficiencyTechnician);
                }else if (efficiencyTechnician == maxEfficiency) {
                    technicianEfficiency.put(techId, efficiencyTechnician);
                }
            }
            String result = null;
            if (technicianEfficiency.size() >= 2){
                List<String> bestTechIds = new ArrayList<>(technicianEfficiency.keySet());
                Random random = new Random();
                result = bestTechIds.get(random.nextInt(bestTechIds.size()));
            }else {
                result = technicianEfficiency.keySet().iterator().next();
            }
            return result;
        }catch (NoSuchElementException ex){
            return null;
        }
    }

    @Override
    public boolean isTechnicianFree(String id_tech) {
        if(technicianRepository.countBusy(id_tech) == 0){
            return true;
        }
        return false;
    }

    @Override
    public void updateTechnicianBalance(String id_invoice) {
        InvoicesEntity invoicesEntity = invoicesRepository.findById(id_invoice).get();
        float debt = 0;
        for (DetailInvoicesEntity detailInvoicesEntity : invoicesEntity.getDetailInvoicesEntities()){
            if (detailInvoicesEntity.getName().equals("Công thợ")){
                debt = (detailInvoicesEntity.getTotal_price() * 20) / 100;
            }
        }
        if (debt > 0){
            TechnicianEntity technicianEntity = invoicesEntity.getRepairRequestEntity().getTechnicianEntity();
            float newBalance = invoicesEntity.getTotal_amount() - debt;

            //cộng số tiền thợ được hưởng vào trong ví của thợ
            TechnicianWalletEntity technicianWalletEntity = technicianEntity.getTechnicianWalletEntity();
            technicianWalletEntity.setBalance(technicianWalletEntity.getBalance() + newBalance);
            technicianRepository.save(technicianEntity);

            //gửi thông báo đến thợ là khách hàng đã thanh toán thành công
            String title = "Đơn hàng đã được thanh toán";
            String body = "Đơn hàng " + invoicesEntity.getRepairRequestEntity().getId_request() + " đã được thanh toán";
            String type = "PAYMENT_SUCCESS";
            MessageNotifiDTO messageNotifiDTO = new MessageNotifiDTO();
            messageNotifiDTO.setType(type);
            messageNotifiDTO.setTitle(title);
            messageNotifiDTO.setBody(body);
            messageNotifiDTO.setDateTime(LocalDateTime.now());
            webSocketService.sendPrivateUser(technicianEntity.getEmail(), messageNotifiDTO);
            saveNotification(messageNotifiDTO, technicianEntity);
        }
    }

    @Override
    @Scheduled(cron = "0 0 17 * * *")
    public void sendNotificationAboutDebt() {
        List<TechnicianEntity> technicianEntities = technicianRepository.findAll();
        for (TechnicianEntity technicianEntity : technicianEntities){
            if (technicianEntity.getTechnician_debt() > 200000){
                String title = "Công nợ";
                String body = "Đã đến cuối ngày vui lòng thanh toán công nợ";
                String type = "DEBT";
                MessageNotifiDTO messageNotifiDTO = new MessageNotifiDTO();
                messageNotifiDTO.setType(type);
                messageNotifiDTO.setTitle(title);
                messageNotifiDTO.setBody(body);
                messageNotifiDTO.setDateTime(LocalDateTime.now());
                webSocketService.sendPrivateUser(technicianEntity.getEmail(), messageNotifiDTO);
                saveNotification(messageNotifiDTO, technicianEntity);
            }
            if (technicianEntity.getTechnician_debt() > 1000000){
                String title = "Công nợ";
                String body = "Công nợ của bạn đã vượt mức cho phép vui lòng thanh toán để tiếp tục nhận yêu cầu";
                String type = "DEBT";
                MessageNotifiDTO messageNotifiDTO = new MessageNotifiDTO();
                messageNotifiDTO.setType(type);
                messageNotifiDTO.setTitle(title);
                messageNotifiDTO.setBody(body);
                messageNotifiDTO.setDateTime(LocalDateTime.now());
                webSocketService.sendPrivateUser(technicianEntity.getEmail(), messageNotifiDTO);
                saveNotification(messageNotifiDTO, technicianEntity);
            }
        }
    }

    @Override
    public List<TechnicicanDTO> getOutstandingTechnicians() {
        List<TechnicianEntity> technicianEntities = technicianRepository.findAll();
        List<TechnicicanDTO> technicicanDTOS = new ArrayList<>();
        for (TechnicianEntity technicianEntity : technicianEntities) {

            //Tính số sao trung bình của một thợ
            List<RatingEntity> ratingEntities = ratingRepository.findByTechnicianEntity(technicianEntity);
            Integer average_star = 0;
            if (ratingEntities != null) {
                average_star = average_star_tecnician(ratingEntities);
                System.out.println(average_star);
            }

            //nếu technician được 5 sao mới là nổi bật
            if (average_star == 5){
                //Chuyển từ entity sang dto bằng hàm
                TechnicicanDTO technicicanDTO = ConvertEntityToDTO.ToTechnicianDTO(technicianEntity);
                technicicanDTO.setAvatarBase64(ConvertByteToBase64.toBase64(technicianEntity.getAvatar()));
                technicicanDTO.setLevel(technicianEntity.getLevelEntity().getLevel());
                technicicanDTO.setTotal_star(average_star);

                //Vòng lặp để lấy ra danh sách dịch vụ mà thợ có tham gia
                for(ServiceEntity serviceEntity : technicianEntity.getServiceEntities()){
                    TechnicianServiceDTO technicianServiceDTO = new TechnicianServiceDTO();
                    technicianServiceDTO.setName_service(serviceEntity.getName_service());
                    technicianServiceDTO.setIcon(ConvertByteToBase64.toBase64(serviceEntity.getIcon()));
                    technicicanDTO.getTechnicianServiceDTOS().add(technicianServiceDTO);
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

                //Vòng lăp xử lí để lấy ra các role của user
                for (RoleEntity roleEntity : technicianEntity.getRoleEntities()){
                    RoleDTO roleDTO = new RoleDTO();
                    roleDTO.setId_role(roleEntity.getId_role());
                    roleDTO.setRole_name(roleEntity.getRoleName());
                    technicicanDTO.getRoleDTOS().add(roleDTO);
                }

                //Lấy ra lịch làm việc của thợ
                for (TechnicianScheduleEntity technicianScheduleEntity : technicianEntity.getTechnicianScheduleEntityList()){
                    TechnicianScheduleDTO technicianScheduleDTO = new TechnicianScheduleDTO();
                    modelMapper.map(technicianScheduleEntity, technicianScheduleDTO);
                    technicianScheduleDTO.setStatus_code(technicianScheduleEntity.getStatusEntity().getNameStatus());
                    technicianScheduleDTO.setId_technician(technicianScheduleEntity.getTechnicianEntity().getId_user());
                    technicicanDTO.getTechnicianScheduleDTOS().add(technicianScheduleDTO);
                }

                for(RatingEntity ratingEntity : technicianEntity.getRatingEntities()){
                    RatingDTO ratingDTO = new RatingDTO();
                    modelMapper.map(ratingEntity, ratingDTO);
                    ratingDTO.setId_user(ratingEntity.getCustomerEntity().getId_user());
                    ratingDTO.setFull_name(ratingEntity.getCustomerEntity().getFull_name());
                    ratingDTO.setAvatarBase64(ConvertByteToBase64.toBase64(ratingEntity.getCustomerEntity().getAvatar()));
                    technicicanDTO.getRatingDTOS().add(ratingDTO);
                }

//            Tìm kiếm lịch của thợ để lấy trạng thái hiện tại của thợ
                List<TechnicianScheduleEntity> technicianScheduleEntities = technicianScheduleRepository.findByTechnicianEntityAndDateOrderByIdScheduleDesc(technicianEntity, LocalDate.now());
                if(technicianScheduleEntities.size() > 0){
                    for (TechnicianScheduleEntity technicianScheduleEntity : technicianScheduleEntities){
                        if (technicianScheduleEntity.getStatusEntity().getNameStatus().equals("ONLINE")){
                            technicicanDTO.setStatus_technician("ONLINE");
                        }
                    }
                }else{
                    technicicanDTO.setStatus_technician("OFFLINE");
                }

                technicicanDTOS.add(technicicanDTO);
            }
        }
        return technicicanDTOS;
    }

    public void saveNotification(MessageNotifiDTO messageNotifiDTO, UserEntity userEntity){
        //tạo thông báo mới để lưu vào cơ sở dữ liệu
        NotificationTypeEntity notificationTypeEntity = notificationTypeRepository.findByType(messageNotifiDTO.getType());
        NotificationsEntity notificationsEntity = notificationRepository.findByNotificationTypeEntity(notificationTypeEntity);

        NotificationUserEntity userNotify = new NotificationUserEntity();
        StatusEntity statusNotify = statusRepository.findByNameStatus("UNREAD");
        userNotify.setStatusEntity(statusNotify);
        userNotify.setUserEntity(userEntity);
        userNotify.setNotificationsEntity(notificationsEntity);

        //thêm vào notify
        notificationsEntity.getNotificationUserEntities().add(userNotify);
        //lưu vào cơ sở dữ liệu
        notificationRepository.save(notificationsEntity);
    }
}
