package com.example.bookingapp.API;

import com.example.bookingapp.Models.DTO.DataDTO;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.LevelDTO;
import com.example.bookingapp.Models.Request.LevelRequest;
import com.example.bookingapp.Services.LevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class LevelApi {
    @Autowired
    LevelService levelService;
    @GetMapping(value = "/api/admin/level/")
    public ResponseEntity<Object> getAll(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo){
        Page<LevelDTO> levelDTOS = levelService.getAll(pageNo);
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setTotal_page(levelDTOS.getTotalPages());
        dataDTO.setData(levelDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/api/admin/level/id-level={id_level}")
    public ResponseEntity<Object> detailLevel(@PathVariable Long id_level){
        DataDTO dataDTO = new DataDTO();
        Object result = levelService.detailLevel(id_level);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setData(result);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value = "/api/admin/level/")
    public ResponseEntity<Object> createLevel(@RequestBody LevelRequest levelRequest){
        Object result = levelService.createLevel(levelRequest);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PutMapping(value = "/api/admin/level/")
    public ResponseEntity<Object> updateLevel(@RequestBody LevelRequest levelRequest){
        Object result = levelService.updateLevel(levelRequest);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping(value = "/api/admin/level/id-level={id_level}")
    public ResponseEntity<Object> deleteLevel(@PathVariable Long id_level){
        Object result = levelService.deleteLevel(id_level);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
