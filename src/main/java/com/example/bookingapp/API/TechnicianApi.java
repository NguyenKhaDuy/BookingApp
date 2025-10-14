package com.example.bookingapp.API;

import com.example.bookingapp.Models.DTO.DataDTO;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.TechnicicanDTO;
import com.example.bookingapp.Models.Request.SearchByLocationRequest;
import com.example.bookingapp.Services.TechnicianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping(value = "/api/technician/searchlocation")
    public ResponseEntity<DataDTO> getAll(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestBody SearchByLocationRequest searchByLocationRequest){
        Page<TechnicicanDTO> technicicanDTOS = technicianService.searchTechnicianByLocation(pageNo, searchByLocationRequest);
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setTotal_page(technicicanDTOS.getTotalPages());
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setData(technicicanDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/api/technician/id={id_technician}")
    public ResponseEntity<Object> getById(@PathVariable String id_technician){
        Object result = technicianService.getById(id_technician);
        if(result instanceof ErrorDTO){
            return new ResponseEntity<>((ErrorDTO) result, ((ErrorDTO) result).getHttpStatus());
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setData(result);
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/api/technician/searchName/")
    public ResponseEntity<DataDTO> searchByName(@RequestParam(value = "name_technician") String name_technician,
                                                @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo){
        Page<TechnicicanDTO> technicicanDTOS = technicianService.searchTechnicianByName(pageNo, name_technician);
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setTotal_page(technicicanDTOS.getTotalPages());
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setData(technicicanDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/api/technician/searchService/")
    public ResponseEntity<Object> searchByName(@RequestParam(value = "id_service") Long id_service,
                                                @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo){
        Page<TechnicicanDTO> technicicanDTOS = technicianService.searchTechnicianByService(pageNo, id_service);
        if (technicicanDTOS == null){
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setMessage("Can not found service");
            errorDTO.setHttpStatus(HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
        }
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setTotal_page(technicicanDTOS.getTotalPages());
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setData(technicicanDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }
}
