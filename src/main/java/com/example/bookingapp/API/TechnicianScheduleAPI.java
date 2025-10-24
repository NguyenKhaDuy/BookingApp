package com.example.bookingapp.API;

import com.example.bookingapp.Models.DTO.DataDTO;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.RepairRequestDTO;
import com.example.bookingapp.Models.DTO.TechnicianScheduleDTO;
import com.example.bookingapp.Models.Request.TechnicianScheduleRequest;
import com.example.bookingapp.Services.TechnicianScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class TechnicianScheduleAPI {
    @Autowired
    TechnicianScheduleService technicianScheduleService;
    @GetMapping(value = "/api/technician/schedule/id-technician={id}")
    public ResponseEntity<Object> getScheduleByTechnician(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @PathVariable String id){
        ErrorDTO errorDTO = new ErrorDTO();
        Page<TechnicianScheduleDTO> technicianScheduleDTOS = technicianScheduleService.getAllByTechnician(id, pageNo);
        if(technicianScheduleDTOS == null){
            errorDTO.setMessage("Can not found technician");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setTotal_page(technicianScheduleDTOS.getTotalPages());
        dataDTO.setData(technicianScheduleDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @PostMapping(value = "/api/technician/schedule/")
    public ResponseEntity<Object> addSchedule(@RequestBody TechnicianScheduleRequest technicianScheduleRequest){
        Object result = technicianScheduleService.addSchedule(technicianScheduleRequest);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO)result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
