package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.*;
import com.example.bookingapp.Models.DTO.*;
import com.example.bookingapp.Models.Request.*;
import com.example.bookingapp.Repository.*;
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

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    ModelMapper modelMapper;

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
            Integer average_star = average_star_tecnician(ratingEntities);
            technicicanDTO.setTotal_star(average_star);

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

            //Vòng lăp xử lí để lấy ra các role của user
            for (RoleEntity roleEntity : technicianEntity.getRoleEntities()){
                RoleDTO roleDTO = new RoleDTO();
                roleDTO.setId_role(roleEntity.getId_role());
                roleDTO.setRole_name(roleEntity.getRole_name());
                technicicanDTO.getRoleDTOS().add(roleDTO);
            }

//            Tìm kiếm lịch của thợ để lấy trạng thái hiện tại của thợ
            List<TechnicianScheduleEntity> technicianScheduleEntities = technicianScheduleRepository.findByTechnicianEntityAndDateOrderByIdScheduleDesc(technicianEntity, LocalDate.now());
            if(technicianScheduleEntities.size() > 0){
                TechnicianScheduleEntity technicianScheduleEntity = technicianScheduleEntities.get(0);
                StatusEntity statusEntity = statusRepository.findById(technicianScheduleEntity.getStatusEntity().getId_status()).get();
                technicicanDTO.setStatus_technician(statusEntity.getNameStatus());
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

            //Vòng lăp xử lí để lấy ra các role của user
            for (RoleEntity roleEntity : technicianEntity.getRoleEntities()){
                RoleDTO roleDTO = new RoleDTO();
                roleDTO.setId_role(roleEntity.getId_role());
                roleDTO.setRole_name(roleEntity.getRole_name());
                technicicanDTO.getRoleDTOS().add(roleDTO);
            }

//            Tìm kiếm lịch của thợ để lấy trạng thái hiện tại của thợ
            List<TechnicianScheduleEntity> technicianScheduleEntities = technicianScheduleRepository.findByTechnicianEntityAndDateOrderByIdScheduleDesc(technicianEntity, LocalDate.now());
            if(technicianScheduleEntities.size() > 0){
                TechnicianScheduleEntity technicianScheduleEntity = technicianScheduleEntities.get(0);
                StatusEntity statusEntity = statusRepository.findById(technicianScheduleEntity.getStatusEntity().getId_status()).get();
                technicicanDTO.setStatus_technician(statusEntity.getNameStatus());
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

//            Tìm kiếm lịch của thợ để lấy trạng thái hiện tại của thợ
            List<TechnicianScheduleEntity> technicianScheduleEntities = technicianScheduleRepository.findByTechnicianEntityAndDateOrderByIdScheduleDesc(technicianEntity, LocalDate.now());
            if(technicianScheduleEntities.size() > 0){
                TechnicianScheduleEntity technicianScheduleEntity = technicianScheduleEntities.get(0);
                StatusEntity statusEntity = statusRepository.findById(technicianScheduleEntity.getStatusEntity().getId_status()).get();
                technicicanDTO.setStatus_technician(statusEntity.getNameStatus());
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

            //Vòng lăp xử lí để lấy ra các role của user
           for (RoleEntity roleEntity : technicianEntity.getRoleEntities()){
                RoleDTO roleDTO = new RoleDTO();
                roleDTO.setId_role(roleEntity.getId_role());
                roleDTO.setRole_name(roleEntity.getRole_name());
                technicicanDTO.getRoleDTOS().add(roleDTO);
            }

            //            Tìm kiếm lịch của thợ để lấy trạng thái hiện tại của thợ
            List<TechnicianScheduleEntity> technicianScheduleEntities = technicianScheduleRepository.findByTechnicianEntityAndDateOrderByIdScheduleDesc(technicianEntity, LocalDate.now());
            if(technicianScheduleEntities.size() > 0){
                TechnicianScheduleEntity technicianScheduleEntity = technicianScheduleEntities.get(0);
                StatusEntity statusEntity = statusRepository.findById(technicianScheduleEntity.getStatusEntity().getId_status()).get();
                technicicanDTO.setStatus_technician(statusEntity.getNameStatus());
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

                //            Tìm kiếm lịch của thợ để lấy trạng thái hiện tại của thợ
                List<TechnicianScheduleEntity> technicianScheduleEntities = technicianScheduleRepository.findByTechnicianEntityAndDateOrderByIdScheduleDesc(technicianEntity, LocalDate.now());
                if(technicianScheduleEntities.size() > 0){
                    TechnicianScheduleEntity technicianScheduleEntity = technicianScheduleEntities.get(0);
                    StatusEntity statusEntity = statusRepository.findById(technicianScheduleEntity.getStatusEntity().getId_status()).get();
                    technicicanDTO.setStatus_technician(statusEntity.getNameStatus());
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
        MessageDTO messageDTO = new MessageDTO();
        ErrorDTO errorDTO = new ErrorDTO();
        try{
            TechnicianEntity technicianEntity = technicianRepository.findById(technicianProfileRequest.getId_user()).get();
            try{
                modelMapper.map(technicianProfileRequest, technicianEntity);
                technicianEntity.setUpdated_at(LocalDateTime.now());
                technicianRepository.save(technicianEntity);
                messageDTO.setMessage("Success");
                messageDTO.setHttpStatus(HttpStatus.OK);
                return messageDTO;
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
        MessageDTO messageDTO = new MessageDTO();
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
            messageDTO.setMessage("Success");
            messageDTO.setHttpStatus(HttpStatus.OK);
            return messageDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found user");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object addSkill(SkillTechnicianRequest skillTechnicianRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageDTO messageDTO = new MessageDTO();
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
            messageDTO.setMessage("Success");
            messageDTO.setHttpStatus(HttpStatus.OK);
            return messageDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found technician");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Page<SkillDTO> getSkill(String id_user, Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        List<SkillDTO> skillDTOS = new ArrayList<>();
        try {
            TechnicianEntity technicianEntity = technicianRepository.findById(id_user).get();
            Page<SkillEntity> skillEntities = skillRepository.findByTechnicianEntities(technicianEntity, pageable);
            for (SkillEntity skillEntity : skillEntities){
                SkillDTO skillDTO = new SkillDTO();
                modelMapper.map(skillEntity, skillDTO);
                skillDTOS.add(skillDTO);
            }
            return new PageImpl<>(skillDTOS, skillEntities.getPageable(), skillEntities.getTotalElements());
        }catch (NoSuchElementException ex){
            return null;
        }
    }

    @Override
    public Object deleteSkillOfTechnician(SkillTechnicianRequest skillTechnicianRequest) {
        MessageDTO messageDTO = new MessageDTO();
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
                messageDTO.setMessage("Success");
                messageDTO.setHttpStatus(HttpStatus.OK);
            }else {
                errorDTO.setMessage("Skill not contains in list skill of technician");
                errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
            }
            return messageDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found technician");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object addLocation(LocationTechnicianRequest locationTechnicianRequest) {
        ErrorDTO errorDTO = new ErrorDTO();
        MessageDTO messageDTO = new MessageDTO();
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
            messageDTO.setMessage("Success");
            messageDTO.setHttpStatus(HttpStatus.OK);
            return messageDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found technician");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Page<LocationDTO> getLocation(String id_user, Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        List<LocationDTO> locationDTOS = new ArrayList<>();
        try {
            TechnicianEntity technicianEntity = technicianRepository.findById(id_user).get();
            Page<LocationEntity> locationEntities = locationRepository.findByTechnicianEntities(technicianEntity, pageable);
            for (LocationEntity locationEntity : locationEntities){
                LocationDTO locationDTO = new LocationDTO();
                modelMapper.map(locationEntity, locationDTO);
                locationDTOS.add(locationDTO);
            }
            return new PageImpl<>(locationDTOS, locationEntities.getPageable(), locationEntities.getTotalElements());
        }catch (NoSuchElementException ex){
            return null;
        }
    }

    @Override
    public Object deleteLocationOfTechnician(LocationTechnicianRequest locationTechnicianRequest) {
        MessageDTO messageDTO = new MessageDTO();
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
                messageDTO.setMessage("Success");
                messageDTO.setHttpStatus(HttpStatus.OK);
            }else {
                errorDTO.setMessage("Location not contains in list location of technician");
                errorDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
            }
            return messageDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found technician");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

}
