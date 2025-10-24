package com.example.bookingapp.API;

import com.example.bookingapp.Models.DTO.DataDTO;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.MessageDTO;
import com.example.bookingapp.Models.DTO.RepairRequestDTO;
import com.example.bookingapp.Models.Request.DeleteRequest;
import com.example.bookingapp.Services.RepairRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RepairRequestAPI {
    @Autowired
    RepairRequestService repairRequestService;
    @GetMapping(value = "/api/admin/request/")
    public ResponseEntity<DataDTO> getAll(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo){
        Page<RepairRequestDTO> repairRequestDTOS = repairRequestService.getAll(pageNo);
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setTotal_page(repairRequestDTOS.getTotalPages());
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setData(repairRequestDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/api/request/id={id}")
    public ResponseEntity<Object> getById(@PathVariable Long id){
        Object result = repairRequestService.getById(id);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO)result).getHttpStatus());
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setData(result);
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/api/admin/request/status_code={status_code}")
    public ResponseEntity<Object> getByStatus(@PathVariable String status_code, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo){
        ErrorDTO errorDTO = new ErrorDTO();
        Page<RepairRequestDTO> repairRequestDTOS = repairRequestService.getByStatus(pageNo, status_code);
        if(repairRequestDTOS == null){
            errorDTO.setMessage("Can not found status");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setTotal_page(repairRequestDTOS.getTotalPages());
        dataDTO.setData(repairRequestDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @DeleteMapping(value = "/api/request/")
    public ResponseEntity<MessageDTO> deleteRequest(@RequestBody DeleteRequest deleteRequest){
        MessageDTO result = repairRequestService.deleteRequest(deleteRequest);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
