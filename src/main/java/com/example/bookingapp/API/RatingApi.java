package com.example.bookingapp.API;

import com.example.bookingapp.Models.DTO.DataDTO;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.RatingDTO;
import com.example.bookingapp.Services.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RatingApi {
    @Autowired
    RatingService ratingService;
    @GetMapping(value = "api/ratings/technician/id_technician={id}")
    public ResponseEntity<Object> getRatingByTechnician(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @PathVariable String id){
        Object result = ratingService.getAllRatingsByTechnician(pageNo, id);
        if(result instanceof ErrorDTO) {
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        Page<RatingDTO> pageResult = (Page<RatingDTO>) result;
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setTotal_page(pageResult.getTotalPages());
        dataDTO.setData(pageResult.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }
}
