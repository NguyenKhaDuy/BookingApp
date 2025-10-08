package com.example.bookingapp.API;

import com.example.bookingapp.Models.DTO.DataDTO;
import com.example.bookingapp.Models.DTO.TechnicicanDTO;
import com.example.bookingapp.Services.TechnicianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TechnicianApi {
    @Autowired
    TechnicianService technicianService;
    @GetMapping(value = "/api/technician/")
    public ResponseEntity<DataDTO> getAll(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo){
        Page<TechnicicanDTO> technicicanDTOS = technicianService.getAll(pageNo);
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setTotal_page(technicicanDTOS.getTotalPages());
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setData(technicicanDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }
}
