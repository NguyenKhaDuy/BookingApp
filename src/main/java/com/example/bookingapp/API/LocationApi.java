package com.example.bookingapp.API;

import com.example.bookingapp.Models.DTO.DataDTO;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.LocationDTO;
import com.example.bookingapp.Models.Request.LocationRequest;
import com.example.bookingapp.Models.Request.SkillRequest;
import com.example.bookingapp.Services.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class LocationApi {
    @Autowired
    LocationService locationService;
    @GetMapping(value = "/api/location/")
    public ResponseEntity<DataDTO> getAll(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo){
        Page<LocationDTO> locationDTOS = locationService.getAll(pageNo);
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setTotal_page(locationDTOS.getTotalPages());
        dataDTO.setData(locationDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/api/admin/location/id-location={id_location}")
    public ResponseEntity<Object> detailLocation(@PathVariable Long id_location){
        DataDTO dataDTO = new DataDTO();
        Object result = locationService.detailLocation(id_location);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setData(result);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value = "/api/admin/location/")
    public ResponseEntity<Object> createLocation(@RequestBody LocationRequest locationRequest){
        Object result = locationService.createLocation(locationRequest);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PutMapping(value = "/api/admin/location/")
    public ResponseEntity<Object> updateLocation(@RequestBody LocationRequest locationRequest){
        Object result = locationService.updateLocation(locationRequest);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping(value = "/api/admin/location/id-location={id_location}")
    public ResponseEntity<Object> deleteLocation(@PathVariable Long id_location){
        Object result = locationService.deleteLocation(id_location);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
