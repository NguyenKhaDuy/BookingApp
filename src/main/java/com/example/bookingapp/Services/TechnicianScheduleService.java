package com.example.bookingapp.Services;

import com.example.bookingapp.Models.DTO.TechnicianScheduleDTO;
import com.example.bookingapp.Models.Request.TechnicianScheduleRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface TechnicianScheduleService {
    //Hàm tự động thực hiện để cập nhật lại trạng thái của lịch
    void updateExpiredSchedules();
    Page<TechnicianScheduleDTO> getAllByTechnician(String id_technician, Integer pageNo);
    Object addSchedule(TechnicianScheduleRequest technicianScheduleRequest);
    Object updateSchedule(TechnicianScheduleRequest technicianScheduleRequest);
    Object detailSchedule(Long id_schedule);
}
