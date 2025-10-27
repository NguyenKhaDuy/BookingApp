package com.example.bookingapp.Services.Impl;

import com.example.bookingapp.Entity.StatusEntity;
import com.example.bookingapp.Entity.TechnicianEntity;
import com.example.bookingapp.Entity.TechnicianScheduleEntity;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.MessageDTO;
import com.example.bookingapp.Models.DTO.TechnicianScheduleDTO;
import com.example.bookingapp.Models.Request.TechnicianScheduleRequest;
import com.example.bookingapp.Repository.StatusRepository;
import com.example.bookingapp.Repository.TechnicianRepository;
import com.example.bookingapp.Repository.TechnicianScheduleRepository;
import com.example.bookingapp.Services.TechnicianScheduleService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TechnicianScheduleServiceImpl implements TechnicianScheduleService {
    @Autowired
    StatusRepository statusRepository;
    @Autowired
    TechnicianScheduleRepository technicianScheduleRepository;
    @Autowired
    TechnicianRepository technicianRepository;
    @Autowired
    ModelMapper modelMapper;
    @Override
    // Mỗi 5 phút chạy một lần (cron = "0 */5 * * * *")
    @Scheduled(cron = "0 */1 * * * *")
    public void updateExpiredSchedules() {
        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        // Lấy danh sách lịch đã hết hạn
        List<TechnicianScheduleEntity> expiredSchedules =
                technicianScheduleRepository.findExpiredSchedules(today, nowTime);

        if (expiredSchedules.isEmpty()) {
            return;
        }

        // Lấy status OFFLINE
        StatusEntity expiredStatus = statusRepository.findByNameStatus("OFFLINE");

        for (TechnicianScheduleEntity schedule : expiredSchedules) {
            // Nếu chưa ở trạng thái OFFLINE thì cập nhật
            if (!schedule.getStatusEntity().getNameStatus().equals("OFFLINE")) {
                schedule.setStatusEntity(expiredStatus);
                schedule.setUpdated_at(java.time.LocalDateTime.now());
            }
        }

        technicianScheduleRepository.saveAll(expiredSchedules);
        System.out.println("✅ Đã cập nhật " + expiredSchedules.size() + " lịch hết hạn");
    }

    @Override
    public Page<TechnicianScheduleDTO> getAllByTechnician(String id_technician, Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        try {
            TechnicianEntity technicianEntity = technicianRepository.findById(id_technician).get();
            List<TechnicianScheduleDTO> technicianScheduleDTOS = new ArrayList<>();
            Page<TechnicianScheduleEntity> technicianScheduleEntities = technicianScheduleRepository.findByTechnicianEntityOrderByDateDesc(technicianEntity, pageable);
            for (TechnicianScheduleEntity technicianScheduleEntity : technicianScheduleEntities){
                TechnicianScheduleDTO technicianScheduleDTO = new TechnicianScheduleDTO();
                modelMapper.map(technicianScheduleEntity, technicianScheduleDTO);
                technicianScheduleDTO.setId_technician(technicianScheduleEntity.getTechnicianEntity().getId_user());
                technicianScheduleDTO.setStatus_code(technicianScheduleEntity.getStatusEntity().getNameStatus());
                technicianScheduleDTOS.add(technicianScheduleDTO);
            }
            return new PageImpl<>(technicianScheduleDTOS, technicianScheduleEntities.getPageable(), technicianScheduleEntities.getTotalElements());
        }catch (NoSuchElementException ex){
            return null;
        }
    }

    @Override
    public Object addSchedule(TechnicianScheduleRequest technicianScheduleRequest) {
        MessageDTO messageDTO = new MessageDTO();
        ErrorDTO errorDTO = new ErrorDTO();
        try{
            TechnicianEntity technicianEntity = technicianRepository.findById(technicianScheduleRequest.getUser_id()).get();
            TechnicianScheduleEntity technicianScheduleEntity = new TechnicianScheduleEntity();
            modelMapper.map(technicianScheduleRequest, technicianScheduleEntity);
            technicianScheduleEntity.setTechnicianEntity(technicianEntity);
            technicianScheduleEntity.setCreated_at(LocalDateTime.now());
            technicianScheduleEntity.setUpdated_at(LocalDateTime.now());
            //Tìm trạng thái online
            StatusEntity statusEntity = statusRepository.findByNameStatus("ONLINE");
            technicianScheduleEntity.setStatusEntity(statusEntity);
            technicianScheduleRepository.save(technicianScheduleEntity);
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
    public Object updateSchedule(TechnicianScheduleRequest technicianScheduleRequest) {
        MessageDTO messageDTO = new MessageDTO();
        ErrorDTO errorDTO = new ErrorDTO();
        try{
            //tìm kiếm lịch làm thông qua id
            TechnicianScheduleEntity technicianScheduleEntity = technicianScheduleRepository.findById(technicianScheduleRequest.getId_schedule()).get();
           //tiến hành cập nhật lại thông tin
            modelMapper.map(technicianScheduleRequest, technicianScheduleEntity);
            technicianScheduleEntity.setUpdated_at(LocalDateTime.now());
            technicianScheduleRepository.save(technicianScheduleEntity);
            messageDTO.setMessage("Success");
            messageDTO.setHttpStatus(HttpStatus.OK);
            return messageDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found schedule");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }

    @Override
    public Object detailSchedule(Long id_schedule) {
        ErrorDTO errorDTO = new ErrorDTO();
        try {
            TechnicianScheduleDTO technicianScheduleDTO = new TechnicianScheduleDTO();
            TechnicianScheduleEntity technicianScheduleEntity = technicianScheduleRepository.findById(id_schedule).get();
            modelMapper.map(technicianScheduleEntity, technicianScheduleDTO);
            technicianScheduleDTO.setId_technician(technicianScheduleEntity.getTechnicianEntity().getId_user());
            technicianScheduleDTO.setStatus_code(technicianScheduleEntity.getStatusEntity().getNameStatus());
            return technicianScheduleDTO;
        }catch (NoSuchElementException ex){
            errorDTO.setMessage("Can not found schedule");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return errorDTO;
        }
    }
}
