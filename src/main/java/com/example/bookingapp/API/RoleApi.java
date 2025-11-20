package com.example.bookingapp.API;

import com.example.bookingapp.Models.DTO.DataDTO;
import com.example.bookingapp.Models.DTO.RoleDTO;
import com.example.bookingapp.Services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoleApi {
    @Autowired
    RoleService roleService;
    @GetMapping(value = "/api/admin/role/")
    public ResponseEntity<Object> getAll(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo){
        Page<RoleDTO> roleDTOS = roleService.getAll(pageNo);
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setTotal_page(roleDTOS.getTotalPages());
        dataDTO.setData(roleDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }
}
