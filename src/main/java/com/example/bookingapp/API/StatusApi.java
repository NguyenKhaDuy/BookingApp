package com.example.bookingapp.API;

import com.example.bookingapp.Models.DTO.DataDTO;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.LocationDTO;
import com.example.bookingapp.Models.DTO.StatusDTO;
import com.example.bookingapp.Models.Request.LocationRequest;
import com.example.bookingapp.Models.Request.StatusRequest;
import com.example.bookingapp.Services.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class StatusApi {
    @Autowired
    StatusService statusService;
    @GetMapping(value = "/api/admin/status/")
    public ResponseEntity<DataDTO> getAll(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo){
        Page<StatusDTO> statusDTOS = statusService.getAll(pageNo);
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setTotal_page(statusDTOS.getTotalPages());
        dataDTO.setData(statusDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/api/admin/status/id-status={id_status}")
    public ResponseEntity<Object> detailStatus(@PathVariable Long id_status){
        DataDTO dataDTO = new DataDTO();
        Object result = statusService.detailStatus(id_status);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setData(result);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value = "/api/admin/status/")
    public ResponseEntity<Object> createStatus(@RequestBody StatusRequest statusRequest){
        Object result = statusService.createStatus(statusRequest);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping(value = "/api/admin/status/")
    public ResponseEntity<Object> updateStatus(@RequestBody StatusRequest statusRequest){
        Object result = statusService.updateStatus(statusRequest);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping(value = "/api/admin/status/id-status={id_status}")
    public ResponseEntity<Object> deleteStatus(@PathVariable Long id_status){
        Object result = statusService.deleteStatus(id_status);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
