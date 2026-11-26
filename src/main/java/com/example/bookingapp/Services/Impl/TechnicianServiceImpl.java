package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.*;
import com.example.bookingapp.Models.DTO.*;
import com.example.bookingapp.Models.Request.*;
import com.example.bookingapp.Models.Response.GeocodingResponse;
import com.example.bookingapp.Models.Response.MessageResponse;
import com.example.bookingapp.Repository.*;
import com.example.bookingapp.Services.TechnicianService;
import com.example.bookingapp.Services.WebSocketService;
import com.example.bookingapp.Utils.ConvertByteToBase64;
import com.example.bookingapp.Utils.ConvertEntityToDTO;
import com.example.bookingapp.Utils.DistanceUtils;
import com.example.bookingapp.Utils.RandomIdUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

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
    WalletRepository walletRepository;
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
    @Autowired
    LevelRepository levelRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    WalletRepository WalletRepository;
    @Autowired
    WalletTransactionRepository walletTransactionRepository;
    @Autowired
    CustomerVouchersRepository customerVouchersRepository;
    @Autowired
    private VouchersRepository vouchersRepository;

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

            List<TechnicianServiceDTO> technicianServiceDTOS = new ArrayList<>();
            //Vòng lặp để lấy ra danh sách dịch vụ mà thợ có tham gia
            for(ServiceEntity serviceEntity : technicianEntity.getServiceEntities()){
                TechnicianServiceDTO technicianServiceDTO = new TechnicianServiceDTO();
                technicianServiceDTO.setId_service(serviceEntity.getId_service());
                technicianServiceDTO.setName_service(serviceEntity.getNameService());
                technicianServiceDTO.setIcon(ConvertByteToBase64.toBase64(serviceEntity.getIcon()));
                technicianServiceDTOS.add(technicianServiceDTO);
            }
            technicicanDTO.setTechnicianServiceDTOS(technicianServiceDTOS);

            List<LocationTechnicianDTO> locationTechnicianDTOS = new ArrayList<>();
            //Vòng lặp để lấy ra các vị trí thợ hoạt động
            for (LocationEntity locationEntity : technicianEntity.getLocationEntities()) {
                LocationTechnicianDTO locationTechnicianDTO = new LocationTechnicianDTO();
                locationTechnicianDTO.setWard(locationEntity.getWard());
                locationTechnicianDTO.setDistrict(locationEntity.getDistrict());
                locationTechnicianDTO.setConscious(locationEntity.getConscious());
                locationTechnicianDTOS.add(locationTechnicianDTO);
            }
            technicicanDTO.setLocationTechnicianDTOS(locationTechnicianDTOS);

            //Vòng lặp xử lí để lấy ra các kĩ năng của thợ
            for (SkillEntity skillEntity : technicianEntity.getSkillEntities()) {
                technicicanDTO.getNameSkillTechnician().add(skillEntity.getSkill_name());
            }

            List<RoleDTO> roleDTOS = new ArrayList<>();
            //Vòng lăp xử lí để lấy ra các role của user
            for (RoleEntity roleEntity : technicianEntity.getRoleEntities()){
                RoleDTO roleDTO = new RoleDTO();
                roleDTO.setId_role(roleEntity.getId_role());
                roleDTO.setRole_name(roleEntity.getRoleName());
                roleDTOS.add(roleDTO);
            }
            technicicanDTO.setRoleDTOS(roleDTOS);

            List<RatingDTO> ratingDTOS = new ArrayList<>();
            for(RatingEntity ratingEntity : technicianEntity.getRatingEntities()){
                RatingDTO ratingDTO = new RatingDTO();
                modelMapper.map(ratingEntity, ratingDTO);
                ratingDTO.setId_user(ratingEntity.getCustomerEntity().getId_user());
                ratingDTO.setFull_name(ratingEntity.getCustomerEntity().getFull_name());
                ratingDTO.setAvatarBase64(ConvertByteToBase64.toBase64(ratingEntity.getCustomerEntity().getAvatar()));
                ratingDTOS.add(ratingDTO);
            }
            technicicanDTO.setRatingDTOS(ratingDTOS);

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

            List<TechnicianServiceDTO> technicianServiceDTOS = new ArrayList<>();
            //Vòng lặp để lấy ra danh sách dịch vụ mà thợ có tham gia
            for(ServiceEntity serviceEntity : technicianEntity.getServiceEntities()){
                TechnicianServiceDTO technicianServiceDTO = new TechnicianServiceDTO();
                technicianServiceDTO.setId_service(serviceEntity.getId_service());
                technicianServiceDTO.setName_service(serviceEntity.getNameService());
                technicianServiceDTO.setIcon(ConvertByteToBase64.toBase64(serviceEntity.getIcon()));
                technicianServiceDTOS.add(technicianServiceDTO);
            }
            technicicanDTO.setTechnicianServiceDTOS(technicianServiceDTOS);

            List<LocationTechnicianDTO> locationTechnicianDTOS = new ArrayList<>();
            //Vòng lặp để lấy ra các vị trí thợ hoạt động
            for (LocationEntity locationEntity : technicianEntity.getLocationEntities()) {
                LocationTechnicianDTO locationTechnicianDTO = new LocationTechnicianDTO();
                locationTechnicianDTO.setWard(locationEntity.getWard());
                locationTechnicianDTO.setDistrict(locationEntity.getDistrict());
                locationTechnicianDTO.setConscious(locationEntity.getConscious());
                locationTechnicianDTOS.add(locationTechnicianDTO);
            }
            technicicanDTO.setLocationTechnicianDTOS(locationTechnicianDTOS);

            //Vòng lặp xử lí để lấy ra các kĩ năng của thợ
            for (SkillEntity skillEntity : technicianEntity.getSkillEntities()) {
                technicicanDTO.getNameSkillTechnician().add(skillEntity.getSkill_name());
            }

            List<RoleDTO> roleDTOS = new ArrayList<>();
            //Vòng lăp xử lí để lấy ra các role của user
            for (RoleEntity roleEntity : technicianEntity.getRoleEntities()){
                RoleDTO roleDTO = new RoleDTO();
                roleDTO.setId_role(roleEntity.getId_role());
                roleDTO.setRole_name(roleEntity.getRoleName());
                roleDTOS.add(roleDTO);
            }
            technicicanDTO.setRoleDTOS(roleDTOS);

            List<RatingDTO> ratingDTOS = new ArrayList<>();
            for(RatingEntity ratingEntity : technicianEntity.getRatingEntities()){
                RatingDTO ratingDTO = new RatingDTO();
                modelMapper.map(ratingEntity, ratingDTO);
                ratingDTO.setId_user(ratingEntity.getCustomerEntity().getId_user());
                ratingDTO.setFull_name(ratingEntity.getCustomerEntity().getFull_name());
                ratingDTO.setAvatarBase64(ConvertByteToBase64.toBase64(ratingEntity.getCustomerEntity().getAvatar()));
                ratingDTOS.add(ratingDTO);
            }
            technicicanDTO.setRatingDTOS(ratingDTOS);

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
                technicianServiceDTO.setName_service(serviceEntity.getNameService());
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
                technicianServiceDTO.setName_service(serviceEntity.getNameService());
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
    public Page<TechnicicanDTO> searchTechnicianByService(Integer pageNo, String id_service) {
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
                    technicianServiceDTO.setName_service(service.getNameService());
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
    public List<TechnicicanDTO> searchTechnicianByService(String id_service) {
        List<TechnicicanDTO> technicicanDTOS = new ArrayList<>();
        List<TechnicianEntity> technicianEntities = null;
        try{
            ServiceEntity serviceEntity = serviceRepository.findById(id_service).get();
            technicianEntities = technicianRepository.findByServiceEntities(serviceEntity);
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
                    technicianServiceDTO.setName_service(service.getNameService());
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
        return technicicanDTOS;
    }

    @Override
    public List<TechnicicanDTO> searchTechnicianByLocation(SearchByLocationRequest searchByLocationRequest) {
        return List.of();
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

    @Override
    public Object deleteTechnician(String id_technician) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageResponse messageResponse = new MessageResponse();
        try {
            TechnicianEntity technicianEntity = technicianRepository.findById(id_technician).get();
            for (RoleEntity role : technicianEntity.getRoleEntities()) {
                role.getUserEntities().remove(technicianEntity);   // xóa ở owning side
                roleRepository.save(role);
            }

            technicianEntity.getRoleEntities().clear();

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
    public String filterTechnician(LocalTime time, LocalDate date, String service_id, String id_tech, GeocodingResponse geocodingResponse) {
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
            Map<String, Double> resultTech = new HashMap<>();
//            if (technicianEfficiency.size() >= 2){
                List<String> bestTechIds = new ArrayList<>(technicianEfficiency.keySet());
                for (String techId : bestTechIds) {
                    TechnicianEntity technicianEntity = technicianRepository.findById(techId).get();
                    Double latTech = technicianEntity.getLatitude();
                    Double lonTech = technicianEntity.getLongitude();

                    GeocodingResponse.Feature feature = geocodingResponse.getFeatures().get(0);
                    List<Double> coordinates = feature.getGeometry().getCoordinates();
                    double longCus = coordinates.get(0);
                    double latCus = coordinates.get(1);

                    double distance = DistanceUtils.calculateDistance(latTech, lonTech, latCus, longCus);
                    System.out.println("KHOẢNG CÁCH: " + distance);
                    resultTech.put(techId, distance);
                }
                resultTech = resultTech.entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByValue())
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (a, b) -> a,
                                LinkedHashMap::new
                        ));
                result = resultTech.entrySet()
                        .stream()
                        .findFirst()
                        .map(Map.Entry::getKey)
                        .orElse(null);
//            }else {
//                result = technicianEfficiency.keySet().iterator().next();
//            }
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
    @Transactional(rollbackFor = Exception.class)
    public void updateTechnicianBalance(String id_invoice) {

        InvoicesEntity invoicesEntity = invoicesRepository
                .findById(id_invoice)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy hóa đơn"
                ));

        // Kiểm tra giao dịch đã được xử lý chưa
        String idempotencyKey = "INVOICE_TECH_" + id_invoice;

        if (walletTransactionRepository
                .existsByIdempotencyKey(idempotencyKey)) {
            return;
        }

        // Tổng tiền hóa đơn gốc
        double totalAmount =
                invoicesEntity.getTotal_amount() == null
                        ? 0.0
                        : invoicesEntity.getTotal_amount();

        // Đọc số tiền giảm giá đã được lưu trước đó
        double totalDiscount =
                invoicesEntity.getTotal_discount() == null
                        ? 0.0
                        : invoicesEntity.getTotal_discount();

        if (!Double.isFinite(totalAmount) || totalAmount <= 0) {
            throw new RuntimeException(
                    "Tổng tiền hóa đơn không hợp lệ"
            );
        }

        if (!Double.isFinite(totalDiscount)
                || totalDiscount < 0
                || totalDiscount > totalAmount) {

            throw new RuntimeException(
                    "Số tiền giảm giá không hợp lệ"
            );
        }

        CustomerVouchersEntity customerVoucher =
                invoicesEntity.getCustomerVouchersEntity();

        if (customerVoucher == null && totalDiscount > 0) {
            throw new RuntimeException(
                    "Không tìm thấy voucher của hóa đơn"
            );
        }

        // Số tiền khách hàng thanh toán qua VNPay
        double customerPayAmount =
                totalAmount - totalDiscount;

        // Tính tổng tiền công thợ
        double laborCost = 0.0;

        for (DetailInvoicesEntity detail :
                invoicesEntity.getDetailInvoicesEntities()) {

            String name = detail.getName();

            if (name != null
                    && ("Công thợ".equalsIgnoreCase(name.trim())
                    || "Chi phí nhân công".equalsIgnoreCase(name.trim()))) {

                if (detail.getTotal_price() != null) {
                    laborCost += detail.getTotal_price();
                }
            }
        }

        if (!Double.isFinite(laborCost)
                || laborCost < 0
                || laborCost > totalAmount) {

            throw new RuntimeException(
                    "Chi phí nhân công không hợp lệ"
            );
        }

        // Hoa hồng KingTech
        double companyFee = Math.round(laborCost * 0.20);

        // Tiền thợ nhận theo hóa đơn gốc
        double technicianCredit =
                totalAmount - companyFee;

        // Chi phí voucher
        double promotionExpense = totalDiscount;

        // Doanh thu ròng
        double companyRevenue =
                companyFee - promotionExpense;

        // Lấy yêu cầu sửa chữa
        if (invoicesEntity.getRepairRequestEntity() == null) {
            throw new RuntimeException(
                    "Không tìm thấy yêu cầu sửa chữa"
            );
        }

        // Lấy thợ kỹ thuật
        TechnicianEntity technicianEntity =
                invoicesEntity.getRepairRequestEntity()
                        .getTechnicianEntity();

        if (technicianEntity == null) {
            throw new RuntimeException(
                    "Không tìm thấy thợ kỹ thuật"
            );
        }

        // Lấy ví thợ
        WalletEntity walletEntity =
                technicianEntity.getWalletEntity();

        if (walletEntity == null) {
            throw new RuntimeException(
                    "Không tìm thấy ví của kỹ thuật viên"
            );
        }

        double currentBalance =
                walletEntity.getBalance() == null
                        ? 0.0
                        : walletEntity.getBalance();

        if (!Double.isFinite(currentBalance)) {
            throw new IllegalStateException(
                    "Số dư ví thợ không hợp lệ"
            );
        }

        // Cộng tiền vào ví thợ
        walletEntity.setBalance(
                currentBalance + technicianCredit
        );

        walletRepository.save(walletEntity);

        // Lưu lịch sử giao dịch ví thợ
        WalletTransactionEntity transactionTech =
                new WalletTransactionEntity();

        transactionTech.setType(
                WalletTransactionType.DEPOSIT
        );

        transactionTech.setAmount(technicianCredit);

        transactionTech.setCreatedAt(
                LocalDateTime.now()
        );

        transactionTech.setWallet(walletEntity);

        transactionTech.setStatus(
                WalletTransactionStatus.SUCCESS
        );

        transactionTech.setIdempotencyKey(idempotencyKey);

        walletTransactionRepository.save(transactionTech);

        // Cập nhật voucher nếu có sử dụng
        if (customerVoucher != null) {

            long currentUsageCount =
                    customerVoucher.getUsageCount() == null
                            ? 0L
                            : customerVoucher.getUsageCount();

            if (customerVoucher.getUsageLimit() == null
                    || currentUsageCount >= customerVoucher.getUsageLimit()) {
                throw new IllegalStateException(
                        "Voucher đã hết lượt sử dụng, cần đối soát giao dịch"
                );
            }

            long newUsageCount = currentUsageCount + 1L;

            customerVoucher.setUsageCount(newUsageCount);

            customerVoucher.setUsedAt(
                    LocalDateTime.now()
            );

            if (newUsageCount >= customerVoucher.getUsageLimit()) {

                customerVoucher.setStatus(
                        VoucherStatus.USED
                );
            }

            customerVouchersRepository.save(customerVoucher);

            VouchersEntity vouchersEntity = customerVoucher.getVouchersEntity();
            vouchersEntity.setUsedCount(vouchersEntity.getUsedCount() + 1L);
            vouchersRepository.save(vouchersEntity);
        }

        // Cập nhật hóa đơn
        invoicesEntity.setTotal_discount(totalDiscount);

        invoicesEntity.setUpdated_at(LocalDateTime.now());

        invoicesRepository.save(invoicesEntity);

        // Thông tin đối soát
        System.out.println("========== VNPAY PAYMENT ==========");
        System.out.println("ID hóa đơn: " + id_invoice);
        System.out.println("Tổng hóa đơn: " + totalAmount);
        System.out.println("Tiền giảm giá: " + totalDiscount);
        System.out.println("Khách thanh toán: " + customerPayAmount);
        System.out.println("Tiền công thợ: " + laborCost);
        System.out.println("Hoa hồng KingTech: " + companyFee);
        System.out.println("Thợ thực nhận: " + technicianCredit);
        System.out.println("Chi phí khuyến mãi: " + promotionExpense);
        System.out.println("Doanh thu ròng: " + companyRevenue);
        System.out.println("===================================");

        // Gửi thông báo cho thợ
        MessageNotifiDTO messageNotifiDTO =
                new MessageNotifiDTO();

        messageNotifiDTO.setType("PAYMENT_SUCCESS");

        messageNotifiDTO.setTitle(
                "Đơn hàng đã được thanh toán"
        );

        messageNotifiDTO.setBody(
                "Đơn hàng "
                        + invoicesEntity.getRepairRequestEntity()
                        .getId_request()
                        + " đã được thanh toán qua VNPay"
        );

        messageNotifiDTO.setDateTime(
                LocalDateTime.now()
        );

        saveNotificationTechnician(
                messageNotifiDTO,
                technicianEntity
        );

        String technicianEmail = technicianEntity.getEmail();

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {
                        try {
                            webSocketService.sendPrivateUser(
                                    technicianEmail,
                                    messageNotifiDTO
                            );
                        } catch (Exception e) {
                            System.err.println(
                                    "Lỗi gửi thông báo: " + e.getMessage()
                            );
                        }
                    }
                }
        );
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
                    technicianServiceDTO.setName_service(serviceEntity.getNameService());
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

    @Override
    public List<ServiceDTO> getServices(String id_user) {
        List<ServiceDTO> serviceDTOS = new ArrayList<>();
        try {
            TechnicianEntity technicianEntity = technicianRepository.findById(id_user).get();
            List<ServiceEntity> serviceEntities = serviceRepository.findByTechnicianEntities(technicianEntity);
            for (ServiceEntity serviceEntity : serviceEntities){
                ServiceDTO serviceDTO = new ServiceDTO();
                modelMapper.map(serviceEntity, serviceDTO);
                serviceDTO.setIcon(ConvertByteToBase64.toBase64(serviceEntity.getIcon()));
                serviceDTOS.add(serviceDTO);
            }
            return serviceDTOS;
        }catch (NoSuchElementException ex){
            return null;
        }
    }

    @Override
    public Object addService(ServiceTechnicianRequest serviceTechnicianRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageResponse messageResponse = new MessageResponse();
        ServiceEntity serviceEntity = null;
        try {
            //Tìm kiếm thợ
            TechnicianEntity technicianEntity = technicianRepository.findById(serviceTechnicianRequest.getId_user()).get();
            try {
                //tìm kiếm kĩ năng
                serviceEntity = serviceRepository.findById(serviceTechnicianRequest.getId_service()).get();
            }catch (NoSuchElementException ex){
                errorDTO.setMessage("Can not found service");
                errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
                return errorDTO;
            }
            //thêm kĩ năng và thợ vào từng entity
            technicianEntity.getServiceEntities().add(serviceEntity);
            serviceEntity.getTechnicianEntities().add(technicianEntity);
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
    public Object deleteServiceOfTechnician(ServiceTechnicianRequest serviceTechnicianRequest) {
        MessageResponse messageResponse = new MessageResponse();
        ErrorDTO errorDTO = new ErrorDTO();
        ServiceEntity serviceEntity = null;
        try{
            //tìm kiếm thợ
            TechnicianEntity technicianEntity = technicianRepository.findById(serviceTechnicianRequest.getId_user()).get();
            try {
                //tìm kiếm kĩ năng
                serviceEntity = serviceRepository.findById(serviceTechnicianRequest.getId_service()).get();
            }catch (NoSuchElementException ex){
                errorDTO.setMessage("Can not found service");
                errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
                return errorDTO;
            }
            //kiểm tra xem kĩ năng người dùng gửi lên có nằm trong danh sách kĩ năng của thợ hay không
            if(technicianEntity.getServiceEntities().contains(serviceEntity)){
                //tiến hành xóa kĩ năng và thợ ra khỏi danh sách các thợ của kĩ năng
                //và danh sách kĩ năng của thợ
                technicianEntity.getServiceEntities().remove(serviceEntity);
                serviceEntity.getTechnicianEntities().remove(technicianEntity);
                technicianRepository.save(technicianEntity);
                messageResponse.setMessage("Success");
                messageResponse.setHttpStatus(HttpStatus.OK);
            }else {
                errorDTO.setMessage("Service not contains in list service of technician");
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
    public Object updateDebtForTechnician(String id_technician, Long amount) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageResponse messageResponse = new MessageResponse();
        try {
            TechnicianEntity technicianEntity = technicianRepository.findById(id_technician).get();
            Double new_amount = (double) (technicianEntity.getTechnician_debt() - amount);
            technicianEntity.setTechnician_debt(new_amount);
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

    public void saveNotificationTechnician(MessageNotifiDTO messageNotifiDTO, UserEntity userEntity){
        //tạo thông báo mới để lưu vào cơ sở dữ liệu
        NotificationTypeEntity notificationTypeEntity = notificationTypeRepository.findByType(messageNotifiDTO.getType());

        NotificationsEntity notificationsEntity = new NotificationsEntity();
        notificationsEntity.setId_notify(RandomIdUtils.generateRandomId());
        notificationsEntity.setTitle(messageNotifiDTO.getTitle());
        notificationsEntity.setMessage(messageNotifiDTO.getBody());
        notificationsEntity.setNotificationTypeEntity(notificationTypeEntity);
        notificationsEntity.setUpdated_at(LocalDateTime.now());
        notificationsEntity.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notificationsEntity);

        NotificationUserEntity userNotify = new NotificationUserEntity();
        StatusEntity statusNotify = statusRepository.findByNameStatus("UNREAD");
        userNotify.setId_user_notify(RandomIdUtils.generateRandomId());
        userNotify.setStatusEntity(statusNotify);
        userNotify.setDateTime(LocalDateTime.now());
        userNotify.setUserEntity(userEntity);
        userNotify.setNotificationsEntity(notificationsEntity);

        //thêm vào notify
        notificationsEntity.getNotificationUserEntities().add(userNotify);
        //lưu vào cơ sở dữ liệu
        notificationRepository.save(notificationsEntity);
    }

    public void saveNotification(MessageNotifiDTO messageNotifiDTO, UserEntity userEntity){
        //tạo thông báo mới để lưu vào cơ sở dữ liệu
        NotificationTypeEntity notificationTypeEntity = notificationTypeRepository.findByType(messageNotifiDTO.getType());
        NotificationsEntity notificationsEntity = notificationRepository.findByNotificationTypeEntity(notificationTypeEntity);

        NotificationUserEntity userNotify = new NotificationUserEntity();
        StatusEntity statusNotify = statusRepository.findByNameStatus("UNREAD");
        userNotify.setStatusEntity(statusNotify);
        userNotify.setDateTime(LocalDateTime.now());
        userNotify.setUserEntity(userEntity);
        userNotify.setNotificationsEntity(notificationsEntity);

        //thêm vào notify
        notificationsEntity.getNotificationUserEntities().add(userNotify);
        //lưu vào cơ sở dữ liệu
        notificationRepository.save(notificationsEntity);
    }

    @Override
    public Object StatisticRequestCurrentMonth(String idTechnician) {
        StatisticTechnicianDTO statisticTechnicianDTO = new StatisticTechnicianDTO();
        MessageResponse messageResponse = new MessageResponse();
        TechnicianEntity technicianEntity = null;
        Long requestIncompleted = 0L;
        Long requestCompleted = 0L;
        Long requestReceived = 0L;
        Long requestReceiving = 0L;
        Long invoiceUnpaid = 0L;
        Long invoicePaid = 0L;
        try{
            technicianEntity = technicianRepository.findById(idTechnician).get();
        }catch (NoSuchElementException ex){
            messageResponse.setMessage("Can not found technician");
            messageResponse.setHttpStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
        Long currentMonth = (long) LocalDate.now().getMonthValue();
        for (RepairRequestEntity repairRequestEntity : technicianEntity.getRepairRequestEntities()) {
            if (repairRequestEntity.getCreated_at().getYear() == LocalDate.now().getYear()
                    && repairRequestEntity.getCreated_at().getMonthValue() == currentMonth){
                if (repairRequestEntity.getStatusEntity().getNameStatus().equals("INCOMPLETE")){
                    requestIncompleted++;
                }
                if (repairRequestEntity.getStatusEntity().getNameStatus().equals("COMPLETED")){
                    requestCompleted++;
                }
                if (repairRequestEntity.getStatusEntity().getNameStatus().equals("RECEIVED")){
                    requestReceived++;
                }
                if (repairRequestEntity.getStatusEntity().getNameStatus().equals("RECEIVING")){
                    requestReceiving++;
                }
                if (repairRequestEntity.getInvoicesEntity() != null){
                    if (repairRequestEntity.getInvoicesEntity().getStatusEntity().getNameStatus().equals("UNPAID")){
                        invoiceUnpaid++;
                    }
                    if (repairRequestEntity.getInvoicesEntity().getStatusEntity().getNameStatus().equals("PAID")){
                        invoicePaid++;
                    }
                }
            }

        }
        statisticTechnicianDTO.setRequestIncompleted(requestIncompleted);
        statisticTechnicianDTO.setRequestCompleted(requestCompleted);
        statisticTechnicianDTO.setRequestReceived(requestReceived);
        statisticTechnicianDTO.setRequestReceiving(requestReceiving);
        statisticTechnicianDTO.setInvoiceUnpaid(invoiceUnpaid);
        statisticTechnicianDTO.setInvoicePaid(invoicePaid);
        statisticTechnicianDTO.setMonth(currentMonth);
        return statisticTechnicianDTO;
    }

    @Override
    public Object StatisticRevenueCurrentMonth(String idTechnician) {
        MessageResponse messageResponse = new MessageResponse();
        TechnicianEntity technicianEntity = null;
        RevenueDTO revenueDTO = new RevenueDTO();
        Double revenue = (double) 0;
        try{
            technicianEntity = technicianRepository.findById(idTechnician).get();
        }catch (NoSuchElementException ex){
            messageResponse.setMessage("Can not found technician");
            messageResponse.setHttpStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
        Long currentMonth = (long) LocalDate.now().getMonthValue();
        for (RepairRequestEntity repairRequestEntity : technicianEntity.getRepairRequestEntities()) {
            if (repairRequestEntity.getInvoicesEntity() != null){
                if (repairRequestEntity.getInvoicesEntity().getCreated_at().getYear() == LocalDate.now().getYear()
                        && repairRequestEntity.getInvoicesEntity().getCreated_at().getMonthValue() == currentMonth
                        && repairRequestEntity.getInvoicesEntity().getStatusEntity().getNameStatus().equals("PAID")){
                    //Phần tiền mà công ty hưởng
                    Float debt = 0.0F;
                    for (DetailInvoicesEntity detailInvoicesEntity : repairRequestEntity.getInvoicesEntity().getDetailInvoicesEntities()){
                        if (detailInvoicesEntity.getName().equals("Công thợ")){
                            debt = (detailInvoicesEntity.getTotal_price() * 20) / 100;
                        }
                    }
                    revenue = revenue + repairRequestEntity.getInvoicesEntity().getTotal_amount() - debt;
                }
            }
        }
        revenueDTO.setMonth(currentMonth);
        revenueDTO.setTotalRevenue(revenue);
        return revenueDTO;
    }

    @Override
    public Object StatisticRequest(StatisticTechnicianRequest statisticTechnicianRequest) {
        StatisticRequestTechnicianDTO statisticRequestTechnicianDTO = new StatisticRequestTechnicianDTO();
        List<StatisticTechnicianDTO> statisticTechnicianDTOS = new ArrayList<>();
        for (int i = 1; i <= 12; i++){
            StatisticTechnicianDTO statisticTechnicianDTO = new StatisticTechnicianDTO();
            MessageResponse messageResponse = new MessageResponse();
            TechnicianEntity technicianEntity = null;
            Long requestIncompleted = 0L;
            Long requestCompleted = 0L;
            Long requestReceived = 0L;
            Long requestReceiving = 0L;
            Long invoiceUnpaid = 0L;
            Long invoicePaid = 0L;
            try{
                technicianEntity = technicianRepository.findById(statisticTechnicianRequest.getIdTechnician()).get();
            }catch (NoSuchElementException ex){
                messageResponse.setMessage("Can not found technician");
                messageResponse.setHttpStatus(HttpStatus.NOT_FOUND);
                return messageResponse;
            }
            Long currentMonth = (long) i;
            for (RepairRequestEntity repairRequestEntity : technicianEntity.getRepairRequestEntities()) {
                if (repairRequestEntity.getCreated_at().getYear() == statisticTechnicianRequest.getYear()
                        && repairRequestEntity.getCreated_at().getMonthValue() == currentMonth){
                    if (repairRequestEntity.getStatusEntity().getNameStatus().equals("INCOMPLETE")){
                        requestIncompleted++;
                    }
                    if (repairRequestEntity.getStatusEntity().getNameStatus().equals("COMPLETED")){
                        requestCompleted++;
                    }
                    if (repairRequestEntity.getStatusEntity().getNameStatus().equals("RECEIVED")){
                        requestReceived++;
                    }
                    if (repairRequestEntity.getStatusEntity().getNameStatus().equals("RECEIVING")){
                        requestReceiving++;
                    }
                    if (repairRequestEntity.getInvoicesEntity() != null){
                        if (repairRequestEntity.getInvoicesEntity().getStatusEntity().getNameStatus().equals("UNPAID")){
                            invoiceUnpaid++;
                        }
                        if (repairRequestEntity.getInvoicesEntity().getStatusEntity().getNameStatus().equals("PAID")){
                            invoicePaid++;
                        }
                    }
                }

            }
            statisticTechnicianDTO.setRequestIncompleted(requestIncompleted);
            statisticTechnicianDTO.setRequestCompleted(requestCompleted);
            statisticTechnicianDTO.setRequestReceived(requestReceived);
            statisticTechnicianDTO.setRequestReceiving(requestReceiving);
            statisticTechnicianDTO.setInvoiceUnpaid(invoiceUnpaid);
            statisticTechnicianDTO.setInvoicePaid(invoicePaid);
            statisticTechnicianDTO.setMonth(currentMonth);
            statisticTechnicianDTOS.add(statisticTechnicianDTO);
        }
        statisticRequestTechnicianDTO.setYear(statisticTechnicianRequest.getYear());
        statisticRequestTechnicianDTO.setStatisticTechnicianDTOS(statisticTechnicianDTOS);
        return statisticRequestTechnicianDTO;
    }

    @Override
    public Object StatisticRevenue(StatisticTechnicianRequest statisticTechnicianRequest) {
        RevenueTechnicianDTO revenueTechnicianDTO = new RevenueTechnicianDTO();
        List<RevenueDTO> revenueDTOS = new ArrayList<>();
        for (int i = 1; i <= 12; i++){
            MessageResponse messageResponse = new MessageResponse();
            TechnicianEntity technicianEntity = null;
            RevenueDTO revenueDTO = new RevenueDTO();
            Double revenue = (double)0;
            try{
                technicianEntity = technicianRepository.findById(statisticTechnicianRequest.getIdTechnician()).get();
            }catch (NoSuchElementException ex){
                messageResponse.setMessage("Can not found technician");
                messageResponse.setHttpStatus(HttpStatus.NOT_FOUND);
                return messageResponse;
            }
            Long currentMonth = (long) i;
            for (RepairRequestEntity repairRequestEntity : technicianEntity.getRepairRequestEntities()) {
                if (repairRequestEntity.getInvoicesEntity() != null){
                    if (repairRequestEntity.getInvoicesEntity().getCreated_at().getYear() == statisticTechnicianRequest.getYear()
                            && repairRequestEntity.getInvoicesEntity().getCreated_at().getMonthValue() == currentMonth
                            && repairRequestEntity.getInvoicesEntity().getStatusEntity().getNameStatus().equals("PAID")){
                        Float debt = 0.0F;
                        for (DetailInvoicesEntity detailInvoicesEntity : repairRequestEntity.getInvoicesEntity().getDetailInvoicesEntities()){
                            if (detailInvoicesEntity.getName().equals("Công thợ")){
                                debt = (detailInvoicesEntity.getTotal_price() * 20) / 100;
                            }
                        }
                        revenue = revenue + repairRequestEntity.getInvoicesEntity().getTotal_amount() - debt;
                    }
                }
            }
            revenueDTO.setMonth(currentMonth);
            revenueDTO.setTotalRevenue(revenue);
            revenueDTOS.add(revenueDTO);
        }
        revenueTechnicianDTO.setYear(statisticTechnicianRequest.getYear());
        revenueTechnicianDTO.setRevenueDTOS(revenueDTOS);
        return revenueTechnicianDTO;
    }

    @Override
    public MessageResponse updateLevel() {
        List<TechnicianEntity> technicianEntities = technicianRepository.findAll();
        for (TechnicianEntity technicianEntity : technicianEntities){
            LevelEntity levelEntity = null;
            if (technicianEntity.getExperience_year() >= 0 && technicianEntity.getExperience_year() < 3){
                levelEntity = levelRepository.findByLevel("Junior");
            }else if (technicianEntity.getExperience_year() >=3 && technicianEntity.getExperience_year() <= 7){
                levelEntity = levelRepository.findByLevel("Mid-level");
            }else {
                levelEntity = levelRepository.findByLevel("Senior");
            }
            technicianEntity.setLevelEntity(levelEntity);
            technicianRepository.save(technicianEntity);
        }
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setHttpStatus(HttpStatus.OK);
        messageResponse.setMessage("Success");
        return messageResponse;
    }

    @Override
    public void updateLocationTechnician(TechnicianLocationRequest request, UserDetails userDetails) {
        System.out.println(userDetails.getUsername());
        TechnicianEntity technician =
                technicianRepository.findByEmail(userDetails.getUsername());

        technician.setLatitude(request.getLatitude());
        technician.setLongitude(request.getLongitude());

        technicianRepository.save(technician);
    }

    @Override
    public Object paymentDebt(PaymentDebtByKingTechPayRequest paymentDebtByKingTechPayRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageResponse messageResponse = new MessageResponse();
        TechnicianEntity technicianEntity = null;
        try {
            technicianEntity = technicianRepository.findById(paymentDebtByKingTechPayRequest.getTechnicianId()).get();
        }catch (Exception e){
            errorDTO.setMessage("Can not found technician");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
        WalletEntity walletEntity = technicianEntity.getWalletEntity();
        walletEntity.setBalance(walletEntity.getBalance() - paymentDebtByKingTechPayRequest.getAmount());
        walletRepository.save(walletEntity);

        WalletTransactionEntity walletTransactionEntity = new WalletTransactionEntity();
        walletTransactionEntity.setType(WalletTransactionType.WITHDRAW);
        walletTransactionEntity.setAmount(paymentDebtByKingTechPayRequest.getAmount());
        walletTransactionEntity.setCreatedAt(LocalDateTime.now());
        walletTransactionEntity.setWallet(walletEntity);
        walletTransactionEntity.setStatus(WalletTransactionStatus.SUCCESS);
        walletTransactionEntity.setIdempotencyKey(UUID.randomUUID().toString());
        walletTransactionRepository.save(walletTransactionEntity);

        technicianEntity.setTechnician_debt(technicianEntity.getTechnician_debt() - paymentDebtByKingTechPayRequest.getAmount());
        technicianRepository.save(technicianEntity);

        messageResponse.setMessage("Success");
        messageResponse.setHttpStatus(HttpStatus.OK);
        return messageResponse;
    }
}
