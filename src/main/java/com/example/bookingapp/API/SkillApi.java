package com.example.bookingapp.API;

import com.example.bookingapp.Models.DTO.DataDTO;
import com.example.bookingapp.Models.DTO.ErrorDTO;
import com.example.bookingapp.Models.DTO.RoleDTO;
import com.example.bookingapp.Models.DTO.SkillDTO;
import com.example.bookingapp.Models.Request.RoleRequest;
import com.example.bookingapp.Models.Request.SkillRequest;
import com.example.bookingapp.Services.SkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class SkillApi {
    @Autowired
    SkillService skillService;
    @GetMapping(value = "/api/admin/skill/")
    public ResponseEntity<Object> getAll(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo){
        Page<SkillDTO> skillDTOS = skillService.getAll(pageNo);
        DataDTO dataDTO = new DataDTO();
        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setCurrent_page(pageNo);
        dataDTO.setTotal_page(skillDTOS.getTotalPages());
        dataDTO.setData(skillDTOS.getContent());
        return new ResponseEntity<>(dataDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/api/admin/skill/id-skill={id_skill}")
    public ResponseEntity<Object> detailSkill(@PathVariable Long id_skill){
        DataDTO dataDTO = new DataDTO();
        Object result = skillService.detailSkill(id_skill);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        dataDTO.setMessage("Success");
        dataDTO.setHttpStatus(HttpStatus.OK);
        dataDTO.setData(result);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value = "/api/admin/skill/")
    public ResponseEntity<Object> createSkill(@RequestBody SkillRequest skillRequest){
        Object result = skillService.createSkill(skillRequest);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping(value = "/api/admin/skill/")
    public ResponseEntity<Object> updateSkill(@RequestBody SkillRequest skillRequest){
        Object result = skillService.updateSkill(skillRequest);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping(value = "/api/admin/skill/id-skill={id_skill}")
    public ResponseEntity<Object> deleteSkill(@PathVariable Long id_skill){
        Object result = skillService.deleteSkill(id_skill);
        if (result instanceof ErrorDTO){
            return new ResponseEntity<>(result, ((ErrorDTO) result).getHttpStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
