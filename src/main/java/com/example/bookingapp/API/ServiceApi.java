package com.example.bookingapp.API;

import com.example.bookingapp.Models.DTO.DataDTO;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.ServiceDTO;
import com.example.bookingapp.Models.Request.NotificationRequest;
import com.example.bookingapp.Models.Request.ServiceRequest;
import com.example.bookingapp.Services.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ServiceApi {
    @Autowired
    ServiceService serviceService;
    @GetMapping(value = "/api/service/")
    public ResponseEntity<DataDTO> getAll(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo){
        Page<ServiceDTO> serviceDTOS = serviceService.getAll(pageNo);
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setTotal_page(serviceDTOS.getTotalPages());
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setData(serviceDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/api/admin/service/id={id}")
    public ResponseEntity<Object> getById(@PathVariable Long id){
        Object result = serviceService.getById(id);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO) result).getHttpStatus());
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setData(result);
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @PostMapping(value = "/api/admin/service/")
    public ResponseEntity<Object> createService(@RequestBody ServiceRequest serviceRequest){
        Object result = serviceService.createService(serviceRequest);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO)result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping(value = "/api/admin/service/")
    public ResponseEntity<Object> updateService(@RequestBody ServiceRequest serviceRequest){
        Object result = serviceService.updateService(serviceRequest);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO)result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping(value = "/api/admin/service/id={id_service}")
    public ResponseEntity<Object> deleteNotification(@PathVariable Long id_service){
        Object result = serviceService.deleteService(id_service);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO)result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
